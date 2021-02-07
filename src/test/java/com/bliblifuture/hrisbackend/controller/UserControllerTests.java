package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveQuotaFormRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveQuotaFormResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveReportResponse;
import com.bliblifuture.hrisbackend.model.response.UserReportResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = UserController.class)
public class UserControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private UserController userController;

    String username = "username@mail.com";

    String empId = "EMP-123";

    Principal principal;

    User user;

    @Before
    public void setup(){
        user = User.builder().username(username).employeeId(empId).build();

        principal = new Principal() {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    public void getUserTest(){
        UserResponse data = UserResponse.builder()
                .username(username)
                .employeeId(empId)
                .build();

        Mockito.when(commandExecutor.execute(GetCurrentUserCommand.class, principal.getName()))
                .thenReturn(Mono.just(data));

        Response<UserResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getUser(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetCurrentUserCommand.class, username);
    }

    @Test
    @SneakyThrows
    public void getLeavesQuotaTest(){
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-1-1");
        LeaveResponse annualLeave = LeaveResponse.builder()
                .used(1).remaining(11)
                .expiries(Arrays.asList(date1))
                .build();
        LeaveResponse extraLeave = LeaveResponse.builder()
                .used(1).remaining(2)
                .expiries(Arrays.asList(date1))
                .build();
        List<LeaveResponse> data = Arrays.asList(annualLeave, extraLeave);

        Mockito.when(commandExecutor.execute(GetLeavesQuotaCommand.class, empId))
                .thenReturn(Mono.just(data));

        Response<List<LeaveResponse>> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getLeavesQuota(empId)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < response.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetLeavesQuotaCommand.class, empId);
    }

    @Test
    @SneakyThrows
    public void getLeavesQuotaFormTest(){
        String code = "ANNUAL_LEAVE";
        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder().code(code).build();
        request.setRequester(principal.getName());

        LeaveQuotaFormResponse data = LeaveQuotaFormResponse.builder()
                .leaveQuota(10)
                .build();

        Mockito.when(commandExecutor.execute(GetLeaveQuotaFormCommand.class, request))
                .thenReturn(Mono.just(data));

        Response<LeaveQuotaFormResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getLeavesQuotaForm(code, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetLeaveQuotaFormCommand.class, request);
    }

    @Test
    public void getLeavesReportTest(){
        LeavesDataSummaryResponse leavesSummary = LeavesDataSummaryResponse.builder()
                .approved(LeavesDataResponse.builder().sick(2).build())
                .pending(LeavesDataResponse.builder().unpaidLeave(1).build())
                .build();
        LeaveQuotaResponse quotaResponse = LeaveQuotaResponse.builder()
                .annual(10)
                .extra(1)
                .substitute(0)
                .build();
        LeaveReportResponse data = LeaveReportResponse.builder()
                .attendance(20)
                .leave(leavesSummary)
                .quota(quotaResponse)
                .build();

        Mockito.when(commandExecutor.execute(GetLeavesReportCommand.class, empId))
                .thenReturn(Mono.just(data));

        Response<LeaveReportResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getLeavesReport(empId)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetLeavesReportCommand.class, empId);
    }

    @Test
    public void getAvailableRequestsTest(){
        List<RequestType> requestTypes = Arrays.asList(
                RequestType.ANNUAL_LEAVE, RequestType.ATTENDANCE, RequestType.SPECIAL_LEAVE,
                RequestType.SUBSTITUTE_LEAVE, RequestType.EXTRA_LEAVE, RequestType.EXTEND_ANNUAL_LEAVE);

        Mockito.when(commandExecutor.execute(GetAvailableRequestsCommand.class, username))
                .thenReturn(Mono.just(requestTypes));

        Response<List<RequestType>> expected = new Response<>();
        expected.setData(requestTypes);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getAvailableRequests(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAvailableRequestsCommand.class, username);
    }

    @Test
    public void getAvailableSpecialRequestsTest(){
        List<SpecialLeaveType> requestTypes = new ArrayList<>();
        requestTypes.add(SpecialLeaveType.SICK);
        requestTypes.add(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER);
        requestTypes.add(SpecialLeaveType.MARRIAGE);
        requestTypes.add(SpecialLeaveType.MATERNITY);
        requestTypes.add(SpecialLeaveType.CHILDBIRTH);
        requestTypes.add(SpecialLeaveType.MAIN_FAMILY_DEATH);
        requestTypes.add(SpecialLeaveType.CLOSE_FAMILY_DEATH);
        requestTypes.add(SpecialLeaveType.HAJJ);
        requestTypes.add(SpecialLeaveType.CHILD_BAPTISM);
        requestTypes.add(SpecialLeaveType.CHILD_CIRCUMSION);
        requestTypes.add(SpecialLeaveType.UNPAID_LEAVE);

        Mockito.when(commandExecutor.execute(GetAvailableSpecialRequestsCommand.class, username))
                .thenReturn(Mono.just(requestTypes));

        Response<List<SpecialLeaveType>> expected = new Response<>();
        expected.setData(requestTypes);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getAvailableSpecialRequests(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAvailableSpecialRequestsCommand.class, username);
    }

    @Test
    public void getAttendanceSummaryTest(){
        AttendanceSummaryResponse month = AttendanceSummaryResponse.builder()
                .absent(2)
                .attendance(15)
                .build();
        AttendanceSummaryResponse year = AttendanceSummaryResponse.builder()
                .absent(10)
                .attendance(220)
                .build();

        UserReportResponse summary = UserReportResponse.builder().month(month).year(year).build();

        Mockito.when(commandExecutor.execute(GetAttendanceSummaryCommand.class, username))
                .thenReturn(Mono.just(summary));

        Response<UserReportResponse> expected = new Response<>();
        expected.setData(summary);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getAttendanceSummmary(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAttendanceSummaryCommand.class, username);
    }


}
