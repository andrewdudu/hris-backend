package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.RequestLeaveStatus;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.RequestLeave;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.RequestLeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GetAttendanceSummaryCommandImpl implements GetAttendanceSummaryCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RequestLeaveRepository requestLeaveRepository;

    @SneakyThrows
    @Override
    public Mono<List<AttendanceSummaryResponse>> execute(String username) {
        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .build();

        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .build();

        List<AttendanceSummaryResponse> responses = Arrays.asList(month, year);

        Date currentDate = new Date();
        int thisMonth = currentDate.getMonth()+1;
        int thisYear = currentDate.getYear()+1900;

        Date startOfCurrentMonth = new SimpleDateFormat("dd/MM/yy")
                .parse(1 + "/" + thisMonth + "/" + thisYear);

        Date startOfCurrentYear = new SimpleDateFormat("dd/MM/yy")
                .parse("1/1/" + thisYear);

        return userRepository.findByUsername(username)
                .flatMap(user -> attendanceRepository.countByDateAfter(startOfCurrentMonth)
                        .flatMap(monthAttendance -> {
                            responses.get(0).setAttendance(monthAttendance);
                            return attendanceRepository.countByDateAfter(startOfCurrentYear);
                        })
                        .flatMap(yearAttendance -> {
                            responses.get(1).setAttendance(yearAttendance);
                            return requestLeaveRepository.findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestLeaveStatus.APPROVED, user.getEmployeeId())
                                    .switchIfEmpty(Flux.just(RequestLeave.builder().dates(new ArrayList<>()).build()))
                                    .collectList();
                        })
                        .map(this::countThisMonthLeave)
                        .flatMap(totalThisMonthLeaves -> {
                            responses.get(0).setAbsence(totalThisMonthLeaves);
                            return employeeLeaveSummaryRepository.findByYearAndEmployeeId(String.valueOf(thisYear), user.getEmployeeId())
                                    .switchIfEmpty(Mono.just(EmployeeLeaveSummary.builder().build()));
                        })
                        .map(this::countThisYearLeaves)
                        .map(totalThisYearLeaves -> {
                            responses.get(1).setAbsence(totalThisYearLeaves);
                            return responses;
                        })
                );
    }

    private int countThisYearLeaves(EmployeeLeaveSummary thisYearLeaves) {
        return thisYearLeaves.getChildBaptism() + thisYearLeaves.getChildBirth()
                + thisYearLeaves.getChildCircumsion() + thisYearLeaves.getCloseFamilyDeath()
                + thisYearLeaves.getHajj() + thisYearLeaves.getMainFamilyDeath()
                + thisYearLeaves.getMaternity() + thisYearLeaves.getSick()
                + thisYearLeaves.getUnpaidLeave();
    }

    private int countThisMonthLeave(List<RequestLeave> thisMonthLeaves) {
        int total = 0;
        for (RequestLeave requestLeave : thisMonthLeaves) {
            total += requestLeave.getDates().size();
        }
        return total;
    }

}
