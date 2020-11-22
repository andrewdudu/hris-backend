package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.repository.*;
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
import java.util.*;

@RunWith(SpringRunner.class)
public class GetAttendaceSummaryCommandImplTests {

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
    private LeaveRequestRepository leaveRequestRepository;

    @Test
    public void test_execute() throws ParseException {

        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        Employee employee = Employee.builder()
                .name("name")
                .email(user.getUsername())
                .build();

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date startOfCurrentMonth = new SimpleDateFormat("dd/MM/yyyy").parse("1/11/2020");

        Integer thisMonthAttendance = 15;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentMonth))
                .thenReturn(Mono.just(thisMonthAttendance));

        Date startOfCurrentYear = new SimpleDateFormat("dd/MM/yyyy").parse("1/1/2020");

        Integer thisYearAttendance = 215;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentYear))
                .thenReturn(Mono.just(thisYearAttendance));

        Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse("3/7/2020");
        Date date2 = new SimpleDateFormat("dd/MM/yyyy").parse("12/8/2020");
        Date date3 = new SimpleDateFormat("dd/MM/yyyy").parse("13/8/2020");

        LeaveRequest leave1 = LeaveRequest.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestLeaveType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date1))
                .build();

        LeaveRequest leave2 = LeaveRequest.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestLeaveType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .dates(Arrays.asList(date2, date3))
                .build();

        Mockito.when(
                leaveRequestRepository.findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestStatus.APPROVED, user.getEmployeeId())
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

        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentMonth);
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), startOfCurrentYear);
        Mockito.verify(leaveRequestRepository, Mockito.times(1)).findByDatesAfterAndStatusAndEmployeeId(startOfCurrentMonth, RequestStatus.APPROVED, user.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1)).findByYearAndEmployeeId(thisYear, user.getEmployeeId());
    }

}
