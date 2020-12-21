package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesReportCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.*;
import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.LeaveReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveQuotaResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataSummaryResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeLeaveSummaryRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
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
import java.util.Date;

@RunWith(SpringRunner.class)
public class GetLeavesReportCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetLeavesReportCommand getLeavesReportCommand(){
            return new GetLeavesReportCommandImpl();
        }
    }

    @Autowired
    private GetLeavesReportCommand getLeavesReportCommand;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .password("pass")
                .build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date lastTimeOfLastYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2019-12-31 23:59:59");
        Date startOfTheYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-1-1");

        String year = "2020";
        Mockito.when(employeeLeaveSummaryRepository.findByYearAndEmployeeId(year, user.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .build();

        leaveSummary.setId("ELS-" + user.getEmployeeId() + "-" + year);
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-13");

        Request request1 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.MAIN_FAMILY_DEATH)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request2 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request3 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request4 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.CLOSE_FAMILY_DEATH)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request5 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.UNPAID_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request6 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.CHILD_BAPTISM)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request7 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.CHILD_CIRCUMSION)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request8 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.CHILDBIRTH)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request9 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.HAJJ)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request10 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.MARRIAGE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();
        Request request11 = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.MATERNITY)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .employeeId(user.getEmployeeId())
                .departmentId("DEP-1")
                .build();

        Mockito.when(requestRepository.findByDatesAfterAndStatusAndEmployeeId(startOfTheYear, RequestStatus.REQUESTED, user.getEmployeeId()))
                .thenReturn(
                        Flux.just(request1, request2, request3, request4, request5,
                                request6, request7, request8, request9, request10, request11)
                );

        Long attendance = 190L;

        Mockito.when(attendanceRepository.countByEmployeeIdAndDateAfter(user.getEmployeeId(), lastTimeOfLastYear))
                .thenReturn(Mono.just(attendance));

        Date startOfNextYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-1-1");
        Leave leave1 = Leave.builder()
                .remaining(10)
                .used(2)
                .expDate(startOfNextYear)
                .employeeId(user.getEmployeeId())
                .type(LeaveType.annual)
                .code("ANNUAL")
                .build();
        Leave leave2 = Leave.builder()
                .remaining(1)
                .used(2)
                .expDate(startOfNextYear)
                .employeeId(user.getEmployeeId())
                .type(LeaveType.extra)
                .code("EXTRA")
                .build();

        Mockito.when(
                leaveRepository.findByEmployeeIdAndExpDateAfterAndTypeOrType(user.getEmployeeId(), lastTimeOfLastYear, LeaveType.annual, LeaveType.extra)
        )
                .thenReturn(Flux.just(leave1, leave2));

        LeavesDataResponse approved = LeavesDataResponse.builder().build();
        BeanUtils.copyProperties(leaveSummary, approved);

        LeavesDataResponse pending = LeavesDataResponse.builder()
                .mainFamilyDeath(2)
                .unpaidLeave(2)
                .sick(4)
                .childBaptism(2)
                .childBirth(2)
                .childCircumsion(2)
                .closeFamilyDeath(2)
                .hajj(2)
                .marriage(2)
                .maternity(2)
                .build();

        LeavesDataSummaryResponse leave = LeavesDataSummaryResponse.builder()
                .approved(approved)
                .pending(pending)
                .build();

        LeaveReportResponse expected = LeaveReportResponse.builder()
                .leave(leave)
                .quota(LeaveQuotaResponse.builder()
                        .annual(10)
                        .extra(1)
                        .build())
                .attendance(Math.toIntExact(attendance))
                .build();

        getLeavesReportCommand.execute(user.getEmployeeId())
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1)).findByYearAndEmployeeId(year, user.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1)).save(leaveSummary);
        Mockito.verify(requestRepository, Mockito.times(1)).findByDatesAfterAndStatusAndEmployeeId(startOfTheYear, RequestStatus.REQUESTED, user.getEmployeeId());
        Mockito.verify(attendanceRepository, Mockito.times(1)).countByEmployeeIdAndDateAfter(user.getEmployeeId(), lastTimeOfLastYear);
        Mockito.verify(leaveRepository, Mockito.times(1)).findByEmployeeIdAndExpDateAfterAndTypeOrType(user.getEmployeeId(), lastTimeOfLastYear, LeaveType.annual, LeaveType.extra);

    }
}
