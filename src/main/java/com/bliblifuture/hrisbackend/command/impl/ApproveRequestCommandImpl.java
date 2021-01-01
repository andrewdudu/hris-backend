package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @SneakyThrows
    @Override
    public Mono<IncomingRequestResponse> execute(BaseRequest data) {
        Date currentDate = dateUtil.getNewDate();
        return requestRepository.findById(data.getId())
                .doOnSuccess(this::checkValidity)
                .map(request -> approvedRequest(data, request, currentDate))
                .flatMap(request -> applyRequest(request, currentDate))
                .flatMap(request -> requestRepository.save(request))
                .flatMap(request -> requestResponseHelper.createResponse(request));
    }

    @SneakyThrows
    private Mono<Request> applyRequest(Request request, Date currentDate){
        LeaveType leaveType;

        String dateString = (currentDate.getYear() + 1900) + "-" + (currentDate.getMonth() + 1) + "-" + currentDate.getDate();
        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        switch (request.getType()){
            case ATTENDANCE:
                return Mono.just(createAttendance(request, currentDate))
                        .flatMap(attendance -> attendanceRepository.save(attendance))
                        .flatMap(attendance -> updateAttendanceReport(startOfDate, currentDate))
                        .map(report -> request);
            case EXTEND_ANNUAL_LEAVE:
                return approveExtendAnnualLeave(request, currentDate);
            case EXTRA_LEAVE:
                leaveType = LeaveType.extra;
                return applyLeave(request, leaveType, currentDate);
            case ANNUAL_LEAVE:
                leaveType = LeaveType.annual;
                return applyLeave(request, leaveType, currentDate);
            case SUBSTITUTE_LEAVE:
                return applySubstituteLeave(request, currentDate);
            case SPECIAL_LEAVE:
                return updateLeaveSummaryAndAttendanceReport(request, currentDate);
            case HOURLY_LEAVE:
                return Mono.just(request);
            default:
                String errorsMessage = "message=INTERNAL_ERROR";
                throw new RuntimeException(errorsMessage);
        }
    }

    private Mono<Request> applyLeave(Request request, LeaveType leaveType, Date currentDate) {
        int dayUsed = request.getDates().size();
        return leaveRepository.findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(
                    request.getEmployeeId(), leaveType, currentDate
                )
                .doOnNext(this::checkNull)
                .map(leave -> updateLeave(leave, dayUsed))
                .flatMap(leave -> leaveRepository.save(leave))
                .flatMap(leave -> updateLeaveSummaryAndAttendanceReport(request, currentDate));
    }

    private Mono<Request> applySubstituteLeave(Request request, Date currentDate) {
        int dayUsed = request.getDates().size();
        return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThan(
                    request.getEmployeeId(), LeaveType.substitute, currentDate, 0
                )
                .collectList()
                .doOnSuccess(leaves -> checkSubstituteLeave(leaves, request.getDates()))
                .map(leaves -> Flux.fromIterable(leaves)
                        .map(leave -> updateLeave(leave, dayUsed))
                        .flatMap(leave -> leaveRepository.save(leave)))
                .flatMap(leave -> updateLeaveSummaryAndAttendanceReport(request, currentDate));
    }

    private void checkSubstituteLeave(List<Leave> leaves, List<Date> dates) {
        if (leaves.size() < dates.size()){
            String errorsMessage = "message=QUOTA_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    @SneakyThrows
    private Mono<Request> updateLeaveSummaryAndAttendanceReport(Request request, Date currentDate) {
        String year = String.valueOf(currentDate.getYear() + 1900);

        return employeeLeaveSummaryRepository.findByYearAndEmployeeId(year, request.getEmployeeId())
                .switchIfEmpty(Mono.just(createLeaveSummary(request.getEmployeeId(), year, currentDate)))
                .map(employeeLeaveSummary -> saveLeaveSummary(employeeLeaveSummary, request))
                .flatMap(employeeLeaveSummary -> employeeLeaveSummaryRepository.save(employeeLeaveSummary))
                .flatMap(employeeLeaveSummary -> applyDailyAttendanceReport(request.getDates(), currentDate))
                .map(reports -> request);
    }

    private Mono<List<DailyAttendanceReport>> applyDailyAttendanceReport(List<Date> dates, Date currentDate) {
        return Flux.fromIterable(dates)
                .flatMap(date -> updateAttendanceReport(date, currentDate))
                .flatMap(report -> dailyAttendanceReportRepository.save(report))
                .collectList();
    }

    private Mono<DailyAttendanceReport> updateAttendanceReport(Date startOfDate, Date currentDate) {
        return dailyAttendanceReportRepository.findByDate(startOfDate)
                .switchIfEmpty(Mono.just(
                        DailyAttendanceReport.builder()
                                .date(startOfDate)
                                .working(0)
                                .absent(0)
                                .build()
                ))
                .map(report -> createOrUpdateAttendanceReport(report, currentDate))
                .flatMap(report -> dailyAttendanceReportRepository.save(report));
    }

    private DailyAttendanceReport createOrUpdateAttendanceReport(DailyAttendanceReport report, Date currentDate) {
        if (report.getId() == null || report.getId().isEmpty()){
            report.setCreatedBy("SYSTEM");
            report.setCreatedDate(currentDate);
            report.setUpdatedBy("SYSTEM");
            report.setUpdatedDate(currentDate);
            report.setId("DAR" + report.getDate().getTime());
        }
        report.setWorking(report.getWorking() + 1);
        return report;
    }

    private EmployeeLeaveSummary saveLeaveSummary(EmployeeLeaveSummary leaveSummary, Request request) {
        if (leaveSummary.getId() == null){
            leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        }

        RequestType type = request.getType();
        int requestDays = request.getDates().size();
        if (type.equals(RequestType.ANNUAL_LEAVE)){
            leaveSummary.setAnnualLeave(leaveSummary.getAnnualLeave() + requestDays);
        }
        else if (type.equals(RequestType.EXTRA_LEAVE)){
            leaveSummary.setExtraLeave(leaveSummary.getExtraLeave() + requestDays);
        }
        else if (type.equals(RequestType.SUBSTITUTE_LEAVE)){
            leaveSummary.setSubstituteLeave(leaveSummary.getSubstituteLeave() + requestDays);
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
        EmployeeLeaveSummary report = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(employeeId)
                .build();

        report.setCreatedBy("SYSTEM");
        report.setUpdatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedDate(currentDate);

        return report;
    }

    private void checkNull(Leave leave) {
        if (leave == null){
            String errorsMessage = "message=LEAVE_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Leave updateLeave(Leave leave, int dayUsed) {
        leave.setRemaining(leave.getRemaining() - dayUsed);
        leave.setUsed(leave.getUsed() + dayUsed);
        return leave;
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
