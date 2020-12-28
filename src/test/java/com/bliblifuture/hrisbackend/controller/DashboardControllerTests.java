package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
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

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = DashboardController.class)
public class DashboardControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private DashboardController dashboardController;

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
    public void loginTest() throws ParseException {
        Date start = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-12 18:00:00");
        DashboardAttendanceResponse attendanceResponse = DashboardAttendanceResponse
                .builder()
                .current(AttendanceResponse.builder()
                        .date(TimeResponse.builder().build())
                        .build())
                .latest(AttendanceResponse.builder()
                        .date(TimeResponse.builder().start(start).end(end).build())
                        .build())
                .build();

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14");
        CalendarResponse calendarResponse = CalendarResponse.builder()
                .status(CalendarStatus.HOLIDAY)
                .date(date1)
                .build();

        ReportResponse reportResponse = ReportResponse.builder()
                .absent(2)
                .working(48)
                .build();

        IncomingRequestTotalResponse requestTotalResponse = IncomingRequestTotalResponse.builder()
                .incoming(4)
                .build();
        DashboardResponse data = DashboardResponse.builder()
                .attendance(attendanceResponse)
                .calendar(calendarResponse)
                .report(reportResponse)
                .request(requestTotalResponse)
                .build();

        Mockito.when(commandExecutor.execute(GetDashboardSummaryCommand.class, principal.getName()))
                .thenReturn(Mono.just(data));

        Response<DashboardResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        dashboardController.getEmployeeDashboard(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetDashboardSummaryCommand.class, principal.getName());
    }

}
