package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
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
import java.util.List;

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

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-15");

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date startOfCurrentMonth = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-1");

        Integer thisMonthAttendance = 15;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentMonth))
                .thenReturn(Mono.just(thisMonthAttendance));

        Date startOfCurrentYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-1-1");

        Integer thisYearAttendance = 215;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentYear))
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
                requestRepository.findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestStatus.APPROVED, user.getEmployeeId())
        ).thenReturn(Flux.just(leave1, leave2));

        String thisYear = "2020";

        EmployeeLeaveSummary summary = EmployeeLeaveSummary.builder()
                .sick(6)
                .childBirth(2)
                .build();

        Mockito.when(employeeLeaveSummaryRepository.findByYearAndEmployeeId(thisYear, user.getEmployeeId()))
                .thenReturn(Mono.just(summary));

        int expectedThisMonthLeaves = 3;

        int expectedThisYearLeaves = 8;

        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .absent(expectedThisMonthLeaves)
                .attendance(thisMonthAttendance)
                .build();

        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .absent(expectedThisYearLeaves)
                .attendance(thisYearAttendance)
                .build();

        List<AttendanceSummaryResponse> expected = Arrays.asList(month, year);

        getAttendanceSummaryCommand.execute(user.getUsername())
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i).getAbsent(), response.get(i).getAbsent());
                        Assert.assertEquals(expected.get(i).getAttendance(), response.get(i).getAttendance());
                    }
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentMonth);
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentYear);
        Mockito.verify(requestRepository, Mockito.times(1)).findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestStatus.APPROVED, user.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1)).findByYearAndEmployeeId(thisYear, user.getEmployeeId());
    }

}
