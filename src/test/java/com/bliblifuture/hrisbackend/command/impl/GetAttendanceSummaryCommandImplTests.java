package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.UserReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
public class GetAttendanceSummaryCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetAttendanceSummaryCommand getAttendanceSummaryCommand(){
            return new GetAttendanceSummaryCommandImpl();
        }
    }

    @Autowired
    private GetAttendanceSummaryCommand getAttendanceSummaryCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {

        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-15");

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date startOfCurrentMonth = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-1");
        Date endOfLastMonth = new Date(startOfCurrentMonth.getTime() - TimeUnit.SECONDS.toMillis(1));

        long thisMonthAttendance = 15;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastMonth))
                .thenReturn(Mono.just(thisMonthAttendance));

        Date startOfCurrentYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-1-1");
        Date endOfLastYear = new Date(startOfCurrentYear.getTime() - TimeUnit.SECONDS.toMillis(1));

        long thisYearAttendance = 215;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastYear))
                .thenReturn(Mono.just(thisYearAttendance));

        Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse("3/7/2020");
        Date date2 = new SimpleDateFormat("dd/MM/yyyy").parse("12/8/2020");
        Date date3 = new SimpleDateFormat("dd/MM/yyyy").parse("13/8/2020");

        Request leave1 = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date1))
                .build();

        Request leave2 = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .dates(Arrays.asList(date2, date3))
                .build();

        Mockito.when(
                requestRepository.findByDatesAfterAndStatusAndEmployeeId(endOfLastMonth, RequestStatus.APPROVED, user.getEmployeeId())
        ).thenReturn(Flux.just(leave1, leave2));

        String thisYear = "2020";

        EmployeeLeaveSummary summary = EmployeeLeaveSummary.builder()
                .sick(6)
                .childBirth(2)
                .build();

        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(thisYear, user.getEmployeeId()))
                .thenReturn(Mono.just(summary));

        int expectedThisMonthLeaves = 3;

        int expectedThisYearLeaves = 8;

        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .absent(expectedThisMonthLeaves)
                .attendance((int) thisMonthAttendance)
                .build();

        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .absent(expectedThisYearLeaves)
                .attendance((int) thisYearAttendance)
                .build();

        UserReportResponse expected = UserReportResponse.builder().month(month).year(year).build();

        getAttendanceSummaryCommand.execute(user.getUsername())
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastMonth);
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), endOfLastYear);
        Mockito.verify(requestRepository, Mockito.times(1)).findByDatesAfterAndStatusAndEmployeeId(endOfLastMonth, RequestStatus.APPROVED, user.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1)).findFirstByYearAndEmployeeId(thisYear, user.getEmployeeId());
    }

}
