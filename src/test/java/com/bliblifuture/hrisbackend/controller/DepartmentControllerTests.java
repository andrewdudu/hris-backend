package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetDepartmentsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.util.DepartmentResponse;
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
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = DepartmentController.class)
public class DepartmentControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private DepartmentController departmentController;

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
    public void getDepartmentsTests() {
        DepartmentResponse dep1 = DepartmentResponse.builder()
                .name("Information Technology")
                .code("DEP-1")
                .build();
        DepartmentResponse dep2 = DepartmentResponse.builder()
                .name("Human Resources")
                .code("DEP-2")
                .build();

        List<DepartmentResponse> data = Arrays.asList(dep1, dep2);

        Mockito.when(commandExecutor.execute(GetDepartmentsCommand.class, ""))
                .thenReturn(Mono.just(data));

        Response<List<DepartmentResponse>> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        departmentController.getDepartments()
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetDepartmentsCommand.class, "");
    }

}
