package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ApproveRequestCommandImpl implements ApproveRequestCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @Autowired
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<RequestResponse> execute(BaseRequest data) {
        Date currentDate = dateUtil.getNewDate();
        return requestRepository.findById(data.getId())
                .doOnSuccess(this::checkValidity)
                .map(request -> approvedRequest(data, request, currentDate))
                .flatMap(request -> applyRequest(request, currentDate))
                .flatMap(request -> requestRepository.save(request))
                .map(this::sendEmail)
                .flatMap(request -> requestResponseHelper.createResponse(request));
    }

    private Request sendEmail(Request request) {
        String emailDest = request.getCreatedBy();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("blibli");
        mail.setTo(emailDest);
        String type = getType(request);
        mail.setSubject(type.replace("_", " ") + " APPROVED");
        mail.setText("Your " + type.toLowerCase().replace("_", " ") + " request has been approved");
        emailSender.send(mail);

        return request;
    }

    private String getType(Request request) {
        RequestType type = request.getType();
        if (type.equals(RequestType.SPECIAL_LEAVE)){
            return request.getSpecialLeaveType().toString().toUpperCase();
        }
        return type.toString().toUpperCase();
    }

    @SneakyThrows
    private Mono<Request> applyRequest(Request request, Date currentDate){
        LeaveType leaveType;

        switch (request.getType()){
            case ATTENDANCE:
                Date reqDate = request.getDates().get(0);
                String dateString = (reqDate.getYear() + 1900) + "-" + (reqDate.getMonth() + 1) + "-" + reqDate.getDate();
                String startTime = " 00:00:00";
                Date startOfRequestDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                        .parse(dateString + startTime);
                return Mono.just(createAttendance(request, currentDate))
                        .flatMap(attendance -> attendanceRepository.save(attendance))
                        .flatMap(attendance -> addWorkingAttendanceReport(startOfRequestDate, currentDate))
                        .flatMap(report -> updateLeaveSummary(request, currentDate));
            case EXTEND_ANNUAL_LEAVE:
                return approveExtendAnnualLeave(request, currentDate)
                        .flatMap(report -> updateLeaveSummary(request, currentDate));
            case EXTRA_LEAVE:
                leaveType = LeaveType.extra;
                return applyLeave(request, leaveType, currentDate)
                        .flatMap(res -> Flux.fromIterable(request.getDates())
                                .flatMap(date -> addAbsentAttendanceReport(date, currentDate))
                                .collectList()
                                .map(reports -> request));
            case ANNUAL_LEAVE:
                leaveType = LeaveType.annual;
                return applyLeave(request, leaveType, currentDate)
                        .flatMap(res -> Flux.fromIterable(request.getDates())
                                .flatMap(date -> addAbsentAttendanceReport(date, currentDate))
                                .collectList()
                                .map(reports -> request));
            case SUBSTITUTE_LEAVE:
                leaveType = LeaveType.substitute;
                return applyLeave(request, leaveType, currentDate)
                        .flatMap(res -> Flux.fromIterable(request.getDates())
                                .flatMap(date -> addAbsentAttendanceReport(date, currentDate))
                                .collectList()
                                .map(reports -> request));
            case SPECIAL_LEAVE:
                return updateLeaveSummary(request, currentDate)
                        .flatMap(res -> Flux.fromIterable(request.getDates())
                                .flatMap(date -> addAbsentAttendanceReport(date, currentDate))
                                .collectList()
                                .map(reports -> request));
            case HOURLY_LEAVE:
                return updateLeaveSummary(request, currentDate);
            default:
                String errorsMessage = "message=INTERNAL_ERROR";
                throw new RuntimeException(errorsMessage);
        }
    }

    private Mono<Request> applyLeave(Request request, LeaveType leaveType, Date currentDate) {
        int dayUsed = request.getDates().size();
        return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                request.getEmployeeId(), leaveType, currentDate, 0
        )
                .switchIfEmpty(Flux.empty())
                .collectList()
                .doOnSuccess(leaves -> checkAvailableQuota(leaves, request.getDates()))
                .flatMap(leaves -> Flux.fromIterable(updateMultipleLeavesQuota(leaves, dayUsed))
                        .flatMap(leave -> leaveRepository.save(leave))
                        .collectList())
                .flatMap(leaves -> updateLeaveSummary(request, currentDate));
    }

    private void checkAvailableQuota(List<Leave> leaves, List<Date> dates) {
        int remaining = 0;
        for (Leave leave : leaves){
            remaining += leave.getRemaining();
        }

        if (remaining < dates.size()){
            String errorsMessage = "message=QUOTA_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Mono<Request> updateLeaveSummary(Request request, Date currentDate) {
        String year = String.valueOf(currentDate.getYear() + 1900);

        return employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId())
                .switchIfEmpty(Mono.just(createLeaveSummary(request.getEmployeeId(), year, currentDate)))
                .map(employeeLeaveSummary -> updateLeaveSummary(employeeLeaveSummary, request))
                .flatMap(employeeLeaveSummary -> employeeLeaveSummaryRepository.save(employeeLeaveSummary))
                .map(leaveSummary -> request);
    }

    private Mono<DailyAttendanceReport> addAbsentAttendanceReport(Date date, Date currentDate) {
        return dailyAttendanceReportRepository.findFirstByDate(date)
                .switchIfEmpty(Mono.just(createNewAttendanceReport(date, currentDate)))
                .map(report -> {
                    report.setAbsent(report.getAbsent() + 1);
                    return report;
                })
                .flatMap(report -> dailyAttendanceReportRepository.save(report));
    }

    private Mono<DailyAttendanceReport> addWorkingAttendanceReport(Date date, Date currentDate) {
        return dailyAttendanceReportRepository.findFirstByDate(date)
                .switchIfEmpty(Mono.just(createNewAttendanceReport(date, currentDate)))
                .map(report -> {
                    report.setWorking(report.getWorking() + 1);
                    return report;
                })
                .flatMap(report -> dailyAttendanceReportRepository.save(report));
    }

    private DailyAttendanceReport createNewAttendanceReport(Date date, Date currentDate){
        DailyAttendanceReport report = DailyAttendanceReport.builder()
                        .date(date)
                        .working(0)
                        .absent(0)
                        .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        return report;
    }

    private EmployeeLeaveSummary updateLeaveSummary(EmployeeLeaveSummary leaveSummary, Request request) {
        if (leaveSummary.getId() == null){
            leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        }

        RequestType type = request.getType();
        int requestDays = 0;
        if (request.getDates() != null){
            requestDays = request.getDates().size();
        }

        if (type.equals(RequestType.ANNUAL_LEAVE)){
            leaveSummary.setAnnualLeave(leaveSummary.getAnnualLeave() + requestDays);
        }
        else if (type.equals(RequestType.EXTRA_LEAVE)){
            leaveSummary.setExtraLeave(leaveSummary.getExtraLeave() + requestDays);
        }
        else if (type.equals(RequestType.SUBSTITUTE_LEAVE)){
            leaveSummary.setSubstituteLeave(leaveSummary.getSubstituteLeave() + requestDays);
        }
        else if (type.equals(RequestType.HOURLY_LEAVE)){
            leaveSummary.setHourlyLeave(leaveSummary.getHourlyLeave() + 1);
        }
        else if (type.equals(RequestType.EXTEND_ANNUAL_LEAVE)){
            leaveSummary.setAnnualLeaveExtension(leaveSummary.getAnnualLeaveExtension() + 1);
        }
        else if (type.equals(RequestType.ATTENDANCE)){
            leaveSummary.setRequestAttendance(leaveSummary.getRequestAttendance() + 1);
        }
        else {
            switch (request.getSpecialLeaveType()){
                case SICK:
                case SICK_WITH_MEDICAL_LETTER:
                    leaveSummary.setSick(leaveSummary.getSick() + requestDays);
                    break;
                case HAJJ:
                    leaveSummary.setHajj(leaveSummary.getHajj() + requestDays);
                    break;
                case MARRIAGE:
                    leaveSummary.setMarriage(leaveSummary.getMarriage() + requestDays);
                    break;
                case MATERNITY:
                    leaveSummary.setMaternity(leaveSummary.getMaternity() + requestDays);
                    break;
                case CHILDBIRTH:
                    leaveSummary.setChildBirth(leaveSummary.getChildBirth() + requestDays);
                    break;
                case UNPAID_LEAVE:
                    leaveSummary.setUnpaidLeave(leaveSummary.getUnpaidLeave() + requestDays);
                    break;
                case CHILD_BAPTISM:
                    leaveSummary.setChildBaptism(leaveSummary.getChildBaptism() + requestDays);
                    break;
                case CHILD_CIRCUMSION:
                    leaveSummary.setChildCircumsion(leaveSummary.getChildCircumsion() + requestDays);
                    break;
                case MAIN_FAMILY_DEATH:
                    leaveSummary.setMainFamilyDeath(leaveSummary.getMainFamilyDeath() + requestDays);
                    break;
                case CLOSE_FAMILY_DEATH:
                    leaveSummary.setCloseFamilyDeath(leaveSummary.getCloseFamilyDeath() + requestDays);
                    break;
            }
        }

        return leaveSummary;
    }

    private EmployeeLeaveSummary createLeaveSummary(String employeeId, String year, Date currentDate) {
        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(employeeId)
                .build();

        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        return leaveSummary;
    }

    private List<Leave> updateMultipleLeavesQuota(List<Leave> leaves, int daysUsed) {
        for (Leave leave : leaves) {
            int remaining = leave.getRemaining();
            if (daysUsed > remaining) {
                leave.setUsed(leave.getUsed() + remaining);
                daysUsed = daysUsed - remaining;
                leave.setRemaining(0);
            }
            else{
                leave.setRemaining(leave.getRemaining() - daysUsed);
                leave.setUsed(leave.getUsed() + daysUsed);
                break;
            }
        }
        return leaves;
    }

    private Attendance createAttendance(Request request, Date currentDate) {
        Attendance attendance = Attendance.builder()
                .startTime(request.getClockIn())
                .endTime(request.getClockOut())
                .locationType(AttendanceLocationType.REQUESTED)
                .date(request.getDates().get(0))
                .employeeId(request.getEmployeeId())
                .build();
        attendance.setCreatedBy(request.getApprovedBy());
        attendance.setCreatedDate(currentDate);
        attendance.setId(uuidUtil.getNewID());
        return attendance;
    }

    @SneakyThrows
    private Mono<Request> approveExtendAnnualLeave(Request request, Date currentDate) {
        Date newExpDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse((currentDate.getYear()+1901) + "-3-1 00:00:00");
        return leaveRepository.findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(request.getEmployeeId(), LeaveType.annual, currentDate)
                .doOnSuccess(this::checkQuota)
                .flatMap(leave -> {
                    leave.setExpDate(newExpDate);
                    return leaveRepository.save(leave);
                })
                .thenReturn(request);
    }

    private void checkQuota(Leave leave) {
        if (leave.getRemaining() < 1){
            String errorsMessage = "message=QUOTA_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private void checkValidity(Request request) {
        if (request == null || request.getStatus().equals(RequestStatus.APPROVED) || request.getStatus().equals(RequestStatus.REJECTED)){
            String errorsMessage = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Request approvedRequest(BaseRequest data, Request request, Date currentDate) {
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(data.getRequester());
        request.setUpdatedBy(data.getRequester());
        request.setUpdatedDate(currentDate);

        return request;
    }

}
