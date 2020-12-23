package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.paging.Paging;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetEmployeeDetailCommand;
import com.bliblifuture.hrisbackend.command.GetEmployeesCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.Gender;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeDetailResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.util.DateUtil;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = EmployeeController.class)
public class EmployeeControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private EmployeeController employeeController;

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
    public void getEmployeesTest() throws ParseException {
        String dep = "DEP-1";
        String name = "a";
        int page = 0;
        int size = 10;
        EmployeesRequest request = new EmployeesRequest();
        request.setDepartment(dep);
        request.setName(name);
        request.setPage(page);
        request.setSize(size);

        EmployeeResponse employeeResponse1 = EmployeeResponse.builder()
                .id("EMP-123")
                .department("Information Tech")
                .name("Employee 1")
                .gender(Gender.MALE)
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .build();

        EmployeeResponse employeeResponse2 = EmployeeResponse.builder()
                .id("EMP-333")
                .department("Information Tech")
                .name("Employee 2")
                .gender(Gender.FEMALE)
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .build();

        List<EmployeeResponse> data = Arrays.asList(employeeResponse1, employeeResponse2);

        Paging paging = Paging.builder()
                .page(request.getPage())
                .itemPerPage(request.getSize())
                .totalItem(data.size())
                .totalPage(1)
                .build();

        PagingResponse<EmployeeResponse> pagingResponse = new PagingResponse<>();
        pagingResponse.setData(data);
        pagingResponse.setPaging(paging);

        Mockito.when(commandExecutor.execute(GetEmployeesCommand.class, request))
                .thenReturn(Mono.just(pagingResponse));

        Response<List<EmployeeResponse>> expected = new Response<>();
        expected.setData(data);
        expected.setPaging(paging);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        employeeController.getEmployees(dep, name, page, size)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                    Assert.assertEquals(expected.getPaging(), response.getPaging());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetEmployeesCommand.class, request);
    }

    @Test
    public void getEmployeeDetail() throws ParseException {
        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00");
        EmployeeDetailResponse data = EmployeeDetailResponse.builder()
                .attendance(AttendanceResponse.builder()
                        .date(TimeResponse.builder().start(date).build())
                        .image("image")
                        .location(LocationResponse.builder().type(AttendanceLocationType.INSIDE).build())
                        .build())
                .user(EmployeeResponse.builder()
                        .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                        .gender(Gender.MALE)
                        .name("Employee 1")
                        .department("InfoTech")
                        .id(user.getEmployeeId())
                        .build())
                .build();

        Mockito.when(commandExecutor.execute(GetEmployeeDetailCommand.class, user.getEmployeeId()))
                .thenReturn(Mono.just(data));

        Response<EmployeeDetailResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        employeeController.getEmployeeDetail(user.getEmployeeId())
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetEmployeeDetailCommand.class, user.getEmployeeId());
    }

}
