package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetAttendanceSummaryCommand;
import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.command.GetAvailableSpecialRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = UserController.class)
public class RequestControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private UserController userController;

    String username = "username@mail.com";

    Principal principal;

    User user;

    @Before
    public void setup(){
        user = User.builder().username(username).build();

        principal = new Principal() {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    public void getAvailableRequestsTest(){
        List<RequestLeaveType> requestLeaveTypes = Arrays.asList(
                RequestLeaveType.ANNUAL_LEAVE, RequestLeaveType.ATTENDANCE, RequestLeaveType.SPECIAL_LEAVE,
                RequestLeaveType.SUBTITUTE_LEAVE, RequestLeaveType.EXTRA_LEAVE, RequestLeaveType.EXTEND_ANNUAL_LEAVE);

        Mockito.when(commandExecutor.execute(GetAvailableRequestsCommand.class, username))
                .thenReturn(Mono.just(requestLeaveTypes));

        Response<List<RequestLeaveType>> expected = new Response<>();
        expected.setData(requestLeaveTypes);
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

        List<AttendanceSummaryResponse> summary = Arrays.asList(month, year);

        Mockito.when(commandExecutor.execute(GetAttendanceSummaryCommand.class, username))
                .thenReturn(Mono.just(summary));

        Response<List<AttendanceSummaryResponse>> expected = new Response<>();
        expected.setData(summary);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        userController.getAttendanceSummmary(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        AttendanceSummaryResponse ex = expected.getData().get(i);
                        AttendanceSummaryResponse res = response.getData().get(i);
                        Assert.assertEquals(ex.getAbsent(), res.getAbsent());
                        Assert.assertEquals(ex.getAttendance(), res.getAttendance());
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAttendanceSummaryCommand.class, username);
    }


}
