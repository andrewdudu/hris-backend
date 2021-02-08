package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.*;
import com.bliblifuture.hrisbackend.model.response.util.*;
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
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class RequestResponseHelperTests {

    @TestConfiguration
    static class command{
        @Bean
        public RequestResponseHelper requestResponseHelper(){
            return new RequestResponseHelper();
        }
    }

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserResponseHelper userResponseHelper;

    @Test
    public void testSpecialLeaveResponse_execute() throws ParseException {
        Request request = Request.builder()
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.UNPAID_LEAVE)
                .departmentId("DEP-123")
                .manager("manager")
                .files(Arrays.asList("file"))
                .approvedBy("manager")
                .notes("notes")
                .status(RequestStatus.APPROVED)
                .dates(Arrays.asList(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-10")))
                .employeeId("emp-123")
                .build();
        request.setId("id123");

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE))
                .build();

        Mockito.when(userRepository.findFirstByEmployeeId(request.getEmployeeId()))
                .thenReturn(Mono.just(user));

        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .leave(LeaveResponse.builder()
                        .remaining(10)
                        .build())
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .department("InfoTech")
                .name("Employee 1")
                .position(PositionResponse.builder().name("Staff").build())
                .build();
        userResponse.setId(user.getEmployeeId());

        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(userResponse));

        List<String> datesString = new ArrayList<>();
        for (Date date : request.getDates()) {
            datesString.add(new SimpleDateFormat(DateUtil.DATE_FORMAT).format(date));
        }

        RequestLeaveDetailResponse leave = RequestLeaveDetailResponse.builder()
                .dates(datesString)
                .files(request.getFiles())
                .notes(request.getNotes())
                .type(request.getSpecialLeaveType().toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leave)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .date(request.getCreatedDate())
                .status(request.getStatus())
                .approvedby(request.getApprovedBy())
                .detail(detail)
                .type(RequestType.LEAVE)
                .user(userResponse)
                .build();
        expected.setId(request.getId());

        requestResponseHelper.createResponse(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByEmployeeId(request.getEmployeeId());
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);

    }

    @Test
    public void testRequestAttendanceResponse_execute() throws ParseException {
        Request request = Request.builder()
                .type(RequestType.ATTENDANCE)
                .departmentId("DEP-123")
                .manager("manager")
                .files(Arrays.asList("file"))
                .approvedBy("manager")
                .notes("notes")
                .status(RequestStatus.APPROVED)
                .clockIn(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 09:00:00"))
                .clockOut(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 18:00:00"))
                .employeeId("emp-123")
                .build();
        request.setId("id123");

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE))
                .build();

        Mockito.when(userRepository.findFirstByEmployeeId(request.getEmployeeId()))
                .thenReturn(Mono.just(user));

        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .leave(LeaveResponse.builder()
                        .remaining(10)
                        .build())
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .department("InfoTech")
                .name("Employee 1")
                .position(PositionResponse.builder().name("Staff").build())
                .build();
        userResponse.setId(user.getEmployeeId());

        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(userResponse));

        TimeResponse dateResponse = TimeResponse.builder()
                .start(request.getClockIn())
                .end(request.getClockOut())
                .build();

        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(dateResponse)
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .date(request.getCreatedDate())
                .status(request.getStatus())
                .approvedby(request.getApprovedBy())
                .detail(detail)
                .type(RequestType.ATTENDANCE)
                .user(userResponse)
                .build();
        expected.setId(request.getId());

        requestResponseHelper.createResponse(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByEmployeeId(request.getEmployeeId());
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);

    }

    @Test
    public void testExtendLeaveResponse_execute() throws ParseException {
        Request request = Request.builder()
                .type(RequestType.EXTEND_ANNUAL_LEAVE)
                .departmentId("DEP-123")
                .manager("manager")
                .approvedBy("manager")
                .notes("notes")
                .status(RequestStatus.APPROVED)
                .employeeId("emp-123")
                .build();
        request.setId("id123");

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE))
                .build();

        Mockito.when(userRepository.findFirstByEmployeeId(request.getEmployeeId()))
                .thenReturn(Mono.just(user));

        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .leave(LeaveResponse.builder()
                        .remaining(10)
                        .build())
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .department("InfoTech")
                .name("Employee 1")
                .position(PositionResponse.builder().name("Staff").build())
                .build();
        userResponse.setId(user.getEmployeeId());

        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(userResponse));

        ExtendLeaveResponse extend = ExtendLeaveResponse.builder()
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .extend(extend)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .date(request.getCreatedDate())
                .status(request.getStatus())
                .approvedby(request.getApprovedBy())
                .detail(detail)
                .type(RequestType.EXTEND)
                .user(userResponse)
                .build();
        expected.setId(request.getId());

        requestResponseHelper.createResponse(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByEmployeeId(request.getEmployeeId());
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);

    }

    @Test
    public void testHourlyLeaveResponse_execute() throws ParseException {
        Request request = Request.builder()
                .type(RequestType.HOURLY_LEAVE)
                .departmentId("DEP-123")
                .manager("manager")
                .files(Arrays.asList("file"))
                .approvedBy("manager")
                .notes("notes")
                .status(RequestStatus.APPROVED)
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 09:00:00"))
                .endTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 11:00:00"))
                .employeeId("emp-123")
                .build();
        request.setId("id123");
        request.setCreatedDate(request.getStartTime());

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE))
                .build();

        Mockito.when(userRepository.findFirstByEmployeeId(request.getEmployeeId()))
                .thenReturn(Mono.just(user));

        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .leave(LeaveResponse.builder()
                        .remaining(10)
                        .build())
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .department("InfoTech")
                .name("Employee 1")
                .position(PositionResponse.builder().name("Staff").build())
                .build();
        userResponse.setId(user.getEmployeeId());

        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(userResponse));

        RequestLeaveDetailResponse leave = RequestLeaveDetailResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType().toString())
                .notes(request.getNotes())
                .dates(Arrays.asList("2020-12-10"))
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leave)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .date(request.getCreatedDate())
                .status(request.getStatus())
                .approvedby(request.getApprovedBy())
                .detail(detail)
                .type(RequestType.LEAVE)
                .user(userResponse)
                .build();
        expected.setId(request.getId());

        requestResponseHelper.createResponse(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByEmployeeId(request.getEmployeeId());
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);

    }

}
