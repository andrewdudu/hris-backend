package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesReportCommand;
import com.bliblifuture.hrisbackend.constant.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.response.LeavesReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveQuotaResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataResponse;
import com.bliblifuture.hrisbackend.repository.*;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GetLeavesReportCommandImpl implements GetLeavesReportCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @SneakyThrows
    @Override
    public Mono<LeavesReportResponse> execute(String employeeId) {
        Date currentDate = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(7));
        int year = currentDate.getYear();

        Date lastTimeOfLastYear = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .parse("31/12/" + (year - 1) + " 16:59:59");

        String theYear = String.valueOf(year);

        LeavesReportResponse response = LeavesReportResponse.builder().build();

        return employeeLeaveSummaryRepository.findByYearAndEmployeeId(theYear, employeeId)
                .switchIfEmpty(Mono.just(createLeaveSummary(employeeId, theYear)))
                .doOnSuccess(report -> checkNewEntity(report))
                .map(employeeLeaveSummary -> setLeavesData(employeeLeaveSummary, response))
                .flatMap(res -> attendanceRepository.countByDateAfter(lastTimeOfLastYear))
                .flatMap(attendance -> {
                    response.setAttendance(attendance);
                    return leaveRepository.findByEmployeeIdAndExpDateAfterAndTypeOrType(employeeId, lastTimeOfLastYear, LeaveType.annual, LeaveType.extra)
                            .collectList();
                })
                .map(leaves -> setRemainingLeaves(leaves, response));
    }

    private LeavesReportResponse setRemainingLeaves(List<Leave> leaves, LeavesReportResponse response) {
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

    private LeavesReportResponse setLeavesData(EmployeeLeaveSummary employeeLeaveSummary, LeavesReportResponse response) {
        LeavesDataResponse leave = LeavesDataResponse.builder().build();
        BeanUtils.copyProperties(employeeLeaveSummary, leave);
        response.setLeave(leave);
        return response;
    }

    private void checkNewEntity(EmployeeLeaveSummary report) {
        report.setId("ELS-" + report.getEmployeeId() + "-" + report.getYear());
        report.setCreatedBy("SYSTEM");
        report.setUpdatedBy("SYSTEM");
        Date date = new Date();
        report.setCreatedDate(date);
        report.setUpdatedDate(date);

        employeeLeaveSummaryRepository.save(report).subscribe();
    }

    private EmployeeLeaveSummary createLeaveSummary(String employeeId, String year) {
        return EmployeeLeaveSummary.builder()
                .childBaptism(0)
                .childBirth(0)
                .childCircumsion(0)
                .closeFamilyDeath(0)
                .hajj(0)
                .mainFamilyDeath(0)
                .maternity(0)
                .sick(0)
                .year(year)
                .employeeId(employeeId)
                .build();
    }

}