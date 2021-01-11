package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.UserReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GetAttendanceSummaryCommandImpl implements GetAttendanceSummaryCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<UserReportResponse> execute(String username) {
        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .build();

        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .build();

        UserReportResponse responses = new UserReportResponse();

        Date currentDate = dateUtil.getNewDate();
        int thisMonth = currentDate.getMonth()+1;
        int thisYear = currentDate.getYear()+1900;

        Date startOfThisMonth = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse(thisYear + "-" + thisMonth + "-" + 1);
        Date endOfLastMonth = new Date(startOfThisMonth.getTime() - TimeUnit.SECONDS.toMillis(1));

        Date startOfThisYear = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse(thisYear + "-1-1");
        Date endOfLastYear = new Date(startOfThisYear.getTime() - TimeUnit.SECONDS.toMillis(1));

        return userRepository.findFirstByUsername(username)
                .flatMap(user -> attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastMonth)
                        .flatMap(monthAttendance -> {
                            month.setAttendance(Math.toIntExact(monthAttendance));
                            return attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastYear);
                        })
                        .flatMap(yearAttendance -> {
                            year.setAttendance(Math.toIntExact(yearAttendance));
                            return requestRepository.findByDatesAfterAndStatusAndEmployeeId(endOfLastMonth, RequestStatus.APPROVED, user.getEmployeeId())
                                    .switchIfEmpty(Flux.empty())
                                    .collectList();
                        })
                        .map(this::countThisMonthLeave)
                        .flatMap(totalThisMonthLeaves -> {
                            month.setAbsent(totalThisMonthLeaves);
                            return employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(String.valueOf(thisYear), user.getEmployeeId())
                                    .switchIfEmpty(Mono.empty());
                        })
                        .map(this::countThisYearLeaves)
                        .map(totalThisYearLeaves -> {
                            year.setAbsent(totalThisYearLeaves);
                            responses.setMonth(month);
                            responses.setYear(year);
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
                + thisYearLeaves.getExtraLeave() + thisYearLeaves.getSubstituteLeave();
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
