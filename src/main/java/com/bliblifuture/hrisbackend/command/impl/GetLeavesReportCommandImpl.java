package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesReportCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.LeaveReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveQuotaResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class GetLeavesReportCommandImpl implements GetLeavesReportCommand {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<LeaveReportResponse> execute(String employeeId) {
        Date currentDate = dateUtil.getNewDate();
        int year = currentDate.getYear() + 1900;

        Date lastTimeOfLastYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse((year-1) + "-12-31" + " 23:59:59");

        String theYear = String.valueOf(year);
        Date startOfTheYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(year + "-1-1");

        LeaveReportResponse response = LeaveReportResponse.builder().build();

        return employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(theYear, employeeId)
                .switchIfEmpty(createLeaveSummary(employeeId, theYear, currentDate)
                        .flatMap(employeeLeaveSummary -> employeeLeaveSummaryRepository.save(employeeLeaveSummary))
                )
                .map(employeeLeaveSummary -> setLeavesData(employeeLeaveSummary, response))
                .flatMap(leaveReportResponse -> requestRepository.findByDatesAfterAndStatusAndEmployeeId(startOfTheYear, RequestStatus.REQUESTED, employeeId)
                        .filter(request -> request.getType().equals(RequestType.SPECIAL_LEAVE))
                        .switchIfEmpty(Flux.empty())
                        .collectList()
                        .map(requests -> countPendingRequestLeave(requests, leaveReportResponse))
                )
                .flatMap(res -> attendanceRepository.countByEmployeeIdAndDateAfter(employeeId, lastTimeOfLastYear))
                .flatMap(attendance -> {
                    response.setAttendance(Math.toIntExact(attendance));
                    return leaveRepository.findByEmployeeIdAndExpDateAfterAndTypeOrType(employeeId, lastTimeOfLastYear, LeaveType.annual, LeaveType.extra)
                            .collectList();
                })
                .map(leaves -> setRemainingLeaves(leaves, response));
    }

    private LeaveReportResponse countPendingRequestLeave(List<Request> requests, LeaveReportResponse leaveReportResponse) {
        LeavesDataResponse pending = LeavesDataResponse.builder().build();
        for (Request request : requests) {
            SpecialLeaveType type = request.getSpecialLeaveType();
            int daysUsed = request.getDates().size();
            switch (type){
                case SICK:
                case SICK_WITH_MEDICAL_LETTER:
                    pending.setSick(pending.getSick() + daysUsed);
                    break;
                case CLOSE_FAMILY_DEATH:
                    pending.setCloseFamilyDeath(pending.getCloseFamilyDeath() + daysUsed);
                    break;
                case MAIN_FAMILY_DEATH:
                    pending.setMainFamilyDeath(pending.getMainFamilyDeath() + daysUsed);
                    break;
                case CHILD_CIRCUMSION:
                    pending.setChildCircumsion(pending.getChildCircumsion() + daysUsed);
                    break;
                case CHILD_BAPTISM:
                    pending.setChildBaptism(pending.getChildBaptism() + daysUsed);
                    break;
                case UNPAID_LEAVE:
                    pending.setUnpaidLeave(pending.getUnpaidLeave() + daysUsed);
                    break;
                case CHILDBIRTH:
                    pending.setChildBirth(pending.getChildBirth() + daysUsed);
                    break;
                case MATERNITY:
                    pending.setMaternity(pending.getMaternity() + daysUsed);
                    break;
                case MARRIAGE:
                    pending.setMarriage(pending.getMarriage() + daysUsed);
                    break;
                case HAJJ:
                    pending.setHajj(pending.getHajj() + daysUsed);
                    break;
            }
        }
        leaveReportResponse.getLeave().setPending(pending);
        return leaveReportResponse;
    }

    private LeaveReportResponse setRemainingLeaves(List<Leave> leaves, LeaveReportResponse response) {
        LeaveQuotaResponse quota = LeaveQuotaResponse.builder().build();
        for (Leave leave: leaves) {
            if (leave.getType().equals(LeaveType.annual)){
                quota.setAnnual(leave.getRemaining());
            }
            else {
                quota.setExtra(leave.getRemaining());
            }
        }
        response.setQuota(quota);
        return response;
    }

    private LeaveReportResponse setLeavesData(EmployeeLeaveSummary employeeLeaveSummary, LeaveReportResponse response) {
        LeavesDataSummaryResponse leave = LeavesDataSummaryResponse.builder().build();
        LeavesDataResponse approved = LeavesDataResponse.builder().build();
        BeanUtils.copyProperties(employeeLeaveSummary, approved);

        leave.setApproved(approved);
        response.setLeave(leave);

        return response;
    }

    private Mono<EmployeeLeaveSummary> createLeaveSummary(String employeeId, String year, Date date) {
        EmployeeLeaveSummary report = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(employeeId)
                .build();

        report.setId("ELS-" + report.getEmployeeId() + "-" + report.getYear());
        report.setCreatedBy("SYSTEM");
        report.setUpdatedBy("SYSTEM");
        report.setCreatedDate(date);
        report.setUpdatedDate(date);

        return Mono.just(report);
    }

}
