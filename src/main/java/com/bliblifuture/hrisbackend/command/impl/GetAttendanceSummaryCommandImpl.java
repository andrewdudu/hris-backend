package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
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
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<List<AttendanceSummaryResponse>> execute(String username) {
        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .build();

        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .build();

        List<AttendanceSummaryResponse> responses = Arrays.asList(month, year);

        Date currentDate = dateUtil.getNewDate();
        int thisMonth = currentDate.getMonth()+1;
        int thisYear = currentDate.getYear()+1900;

        Date startOfCurrentMonth = new SimpleDateFormat("dd/MM/yyyy")
                .parse(1 + "/" + thisMonth + "/" + thisYear);

        Date startOfCurrentYear = new SimpleDateFormat("dd/MM/yyyy")
                .parse("1/1/" + thisYear);

        return userRepository.findByUsername(username)
                .flatMap(user -> attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentMonth)
                        .flatMap(monthAttendance -> {
                            responses.get(0).setAttendance(monthAttendance);
                            return attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentYear);
                        })
                        .flatMap(yearAttendance -> {
                            responses.get(1).setAttendance(yearAttendance);
                            return leaveRequestRepository.findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestStatus.APPROVED, user.getEmployeeId())
                                    .switchIfEmpty(Flux.just(Request.builder().dates(new ArrayList<>()).build()))
                                    .collectList();
                        })
                        .map(this::countThisMonthLeave)
                        .flatMap(totalThisMonthLeaves -> {
                            responses.get(0).setAbsent(totalThisMonthLeaves);
                            return employeeLeaveSummaryRepository.findByYearAndEmployeeId(String.valueOf(thisYear), user.getEmployeeId())
                                    .switchIfEmpty(Mono.just(EmployeeLeaveSummary.builder().build()));
                        })
                        .map(this::countThisYearLeaves)
                        .map(totalThisYearLeaves -> {
                            responses.get(1).setAbsent(totalThisYearLeaves);
                            return responses;
                        })
                );
    }

    private int countThisYearLeaves(EmployeeLeaveSummary thisYearLeaves) {
        return thisYearLeaves.getChildBaptism() + thisYearLeaves.getChildBirth()
                + thisYearLeaves.getChildCircumsion() + thisYearLeaves.getCloseFamilyDeath()
                + thisYearLeaves.getHajj() + thisYearLeaves.getMainFamilyDeath()
                + thisYearLeaves.getMaternity() + thisYearLeaves.getSick()
                + thisYearLeaves.getUnpaidLeave() + thisYearLeaves.getAnnualLeave()
                + thisYearLeaves.getExtraLeave() + thisYearLeaves.getSubtituteLeave();
    }

    private int countThisMonthLeave(List<Request> thisMonthLeaves) {
        int total = 0;
        for (Request request : thisMonthLeaves) {
            if ( !( request.getType().equals(RequestType.ATTENDANCE) || request.getType().equals(RequestType.EXTEND_ANNUAL_LEAVE) ) )
            {
                total += request.getDates().size();
            }
        }
        return total;
    }

}
