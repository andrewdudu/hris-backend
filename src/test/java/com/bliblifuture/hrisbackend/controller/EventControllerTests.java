package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.paging.Paging;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.AddAnnouncementCommand;
import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.command.GetCalendarCommand;
import com.bliblifuture.hrisbackend.command.SetHolidayCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AnnouncementRequest;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
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
    public void getAnnouncementsTest() throws ParseException {
        int page = 0;
        int size = 10;
        PagingRequest request = new PagingRequest(page, size);

        AnnouncementResponse announcementResponse1 = AnnouncementResponse.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12"))
                .notes("Flash Sale")
                .status(CalendarStatus.HOLIDAY)
                .title("Holiday")
                .build();
        AnnouncementResponse announcementResponse2 = AnnouncementResponse.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14"))
                .notes("CEO's Bday")
                .status(CalendarStatus.WORKING)
                .title("CEO's Birthday")
                .build();

        List<AnnouncementResponse> data = Arrays.asList(announcementResponse1, announcementResponse2);

        Paging paging = Paging.builder()
                .itemPerPage(size)
                .page(page)
                .totalItem(data.size())
                .totalPage(1)
                .build();

        PagingResponse<AnnouncementResponse> pagingResponse = new PagingResponse<>();
        pagingResponse.setPaging(paging);
        pagingResponse.setData(data);

        Mockito.when(commandExecutor.execute(GetAnnouncementCommand.class, request))
                .thenReturn(Mono.just(pagingResponse));

        Response<List<AnnouncementResponse>> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        eventController.getAnnouncements(page, size)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAnnouncementCommand.class, request);
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
                .date("2020-12-3")
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

    @Test
    public void addAnnouncementTest() throws ParseException {
        String dateString = "2020-12-30";
        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);

        AnnouncementResponse responseData = AnnouncementResponse.builder()
                .title("Announcement 1")
                .notes("announcement")
                .date(date)
                .build();

        AnnouncementRequest request = AnnouncementRequest.builder()
                .title("Announcement 1")
                .notes("announcement")
                .build();
        request.setRequester(principal.getName());

        Mockito.when(commandExecutor.execute(AddAnnouncementCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<AnnouncementResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        eventController.addAnnouncement(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(AddAnnouncementCommand.class, request);
    }

}
