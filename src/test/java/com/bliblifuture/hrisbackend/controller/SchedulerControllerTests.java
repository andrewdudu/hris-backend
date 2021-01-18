package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.AutoClockoutCommand;
import com.bliblifuture.hrisbackend.command.UpdateLeaveQuotaCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
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
import java.util.Arrays;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = SchedulerController.class)
public class SchedulerControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private SchedulerController schedulerController;

    String username = "username@mail.com";

    Principal principal;

    User user;

    @Before
    public void setup(){
        user = User.builder()
                .username(username)
                .employeeId("EMP-123")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .build();

        principal = new Principal() {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    public void updateLeaveQuotaTest() {
        String res = "[SUCCESS]";
        Mockito.when(commandExecutor.execute(UpdateLeaveQuotaCommand.class, ""))
                .thenReturn(Mono.just(res));

        Response<String> expected = new Response<>();
        expected.setData(res);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        schedulerController.updateLeaveQuota()
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(UpdateLeaveQuotaCommand.class, "");
    }

    @Test
    public void autoClockoutTest() {
        String res = "[SUCCESS]";
        Mockito.when(commandExecutor.execute(AutoClockoutCommand.class, ""))
                .thenReturn(Mono.just(res));

        Response<String> expected = new Response<>();
        expected.setData(res);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        schedulerController.autoClockout()
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(AutoClockoutCommand.class, "");
    }

}
