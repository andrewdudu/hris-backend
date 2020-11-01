package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import com.bliblifuture.hrisbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class GetDashboardSummaryCommandImpl implements GetDashboardSummaryCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceReportRepository attendanceReportRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Mono<DashboardResponse> execute(String username) {
        DashboardResponse response = new DashboardResponse();
        LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String year = String.valueOf(localDate.getYear());
//        return userRepository.findByUsername(username)
//                .flatMap(user -> attendanceReportRepository.findByEmployeeIdAndYear(user.getEmployeeId(), year))
//                .map(res -> {
//                    Report report = Report.builder().working(res.getWorking()).absent(res.getAbsent()).build();
//                    response.setReport(report);
//                });
        return Mono.empty();
    }

//    private void checkNull(User user) {
//        throw new SecurityException("DOES_NOT_MATCH");
//    }

}
