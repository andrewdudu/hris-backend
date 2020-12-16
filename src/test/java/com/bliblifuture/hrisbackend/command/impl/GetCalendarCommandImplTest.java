package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCalendarCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
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
import reactor.core.publisher.Flux;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetCalendarCommandImplTest {

    @TestConfiguration
    static class command{
        @Bean
        public GetCalendarCommand getCalendarCommand(){
            return new GetCalendarCommandImpl();
        }
    }

    @Autowired
    private GetCalendarCommand getCalendarCommand;

    @MockBean
    private EventRepository eventRepository;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        int month = 11;
        int year = 2020;
        GetCalendarRequest request = GetCalendarRequest.builder()
                .month(month)
                .year(year)
                .build();
        request.setRequester(user.getUsername());

        Event event1 = Event.builder().title("Work day").status(CalendarStatus.WORKING)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-2"))
                .build();
        Event event2 = Event.builder().title("Mother's day").status(CalendarStatus.HOLIDAY)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-3"))
                .build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(year + "-" + month + "-1 00:00:00");
        Date before = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(year + "-" + (month+1) + "-1 00:00:00");

        Mockito.when(eventRepository.findByDateBetweenOrderByDateAsc(start, before))
                .thenReturn(Flux.just(event1, event2));

        List<CalendarResponse> expected = new ArrayList<>();
        expected.add(CalendarResponse.builder()
                .events(Arrays.asList(EventDetailResponse.builder()
                        .name("Work day")
                        .build()))
                .status(CalendarStatus.WORKING)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-2"))
                .build());
        expected.add(CalendarResponse.builder()
                .events(Arrays.asList(EventDetailResponse.builder()
                        .name("Mother's day")
                        .build()))
                .status(CalendarStatus.HOLIDAY)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-3"))
                .build());

        getCalendarCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(eventRepository, Mockito.times(1)).findByDateBetweenOrderByDateAsc(start, before);

    }
}
