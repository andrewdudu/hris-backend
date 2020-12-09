package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetCalendarCommand;
import com.bliblifuture.hrisbackend.command.SetHolidayCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = EventController.class)
public class EventControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private EventController eventController;

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
    public void getCalendarTest() throws ParseException {
        int month = 12;
        int year = 2020;
        GetCalendarRequest request = GetCalendarRequest.builder()
                .month(month)
                .year(year)
                .build();

        List<CalendarResponse> responseData = new ArrayList<>();
        responseData.add(CalendarResponse.builder()
                .events(Arrays.asList(EventDetailResponse.builder()
                        .name("Holiday")
                        .build()))
                .status(CalendarStatus.HOLIDAY)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-2"))
                .build());
        responseData.add(CalendarResponse.builder()
                .events(Arrays.asList(EventDetailResponse.builder()
                        .name("Workday")
                        .build()))
                .status(CalendarStatus.WORKING)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-3"))
                .build());

        Mockito.when(commandExecutor.execute(GetCalendarCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<List<CalendarResponse>> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        eventController.getCalendar(month, year)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetCalendarCommand.class, request);
    }

    @Test
    public void setHolidayTest() throws ParseException {
        String dateString = "2020-12-3";
        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
        SetHolidayRequest request = SetHolidayRequest.builder()
                .name("Work day")
                .notes("Work")
                .status(CalendarStatus.WORKING)
                .build();

        EventDetailResponse responseData = EventDetailResponse.builder()
                .name("Work day")
                .status(CalendarStatus.WORKING)
                .notes("Work")
                .build();

        SetHolidayRequest requestCommand = SetHolidayRequest.builder()
                .name("Work day")
                .notes("Work")
                .status(CalendarStatus.WORKING)
                .date(date)
                .build();
        requestCommand.setRequester(principal.getName());

        Mockito.when(commandExecutor.execute(SetHolidayCommand.class, requestCommand))
                .thenReturn(Mono.just(responseData));

        Response<EventDetailResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        eventController.setHoliday(dateString, request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(SetHolidayCommand.class, requestCommand);
    }

}
