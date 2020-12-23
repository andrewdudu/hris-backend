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
        Event event2 = Event.builder().title("CEO day").status(CalendarStatus.WORKING)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-2"))
                .build();
        Event event3 = Event.builder().title("Mother's day").status(CalendarStatus.HOLIDAY)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-3"))
                .build();

        Date startOfThisMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(year + "-" + month + "-1 00:00:00");
        Date StartOfNextMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(year + "-" + (month+1) + "-1 00:00:00");

        Mockito.when(eventRepository.findByDateBetweenOrderByDateAsc(startOfThisMonth, StartOfNextMonth))
                .thenReturn(Flux.just(event1, event2, event3));

        CalendarResponse data1 = CalendarResponse.builder()
                .events(Arrays.asList(
                        EventDetailResponse.builder().name(event1.getTitle()).build(),
                        EventDetailResponse.builder().name(event2.getTitle()).build())
                )
                .status(CalendarStatus.WORKING)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-2"))
                .build();
        CalendarResponse data2 = CalendarResponse.builder()
                .events(Arrays.asList(EventDetailResponse.builder()
                        .name("Mother's day")
                        .build()))
                .status(CalendarStatus.HOLIDAY)
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-3"))
                .build();


        List<CalendarResponse> expected = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Date thisDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-11-"+i);
            if (i == data1.getDate().getDate()){
                expected.add(data1);
                if (thisDate.getDay()==0 || thisDate.getDay()==6){
                    expected.get(i).setStatus(CalendarStatus.HOLIDAY);
                }
            }
            else if (i == data2.getDate().getDate()){
                expected.add(data2);
                if (thisDate.getDay()==0 || thisDate.getDay()==6){
                    expected.get(i).setStatus(CalendarStatus.HOLIDAY);
                }
            }
            else if (thisDate.getDay()==0 || thisDate.getDay()==6){
                CalendarResponse holiday = CalendarResponse.builder()
                        .events(new ArrayList<>())
                        .status(CalendarStatus.HOLIDAY)
                        .date(thisDate)
                        .build();
                expected.add(holiday);
            }
            else {
                CalendarResponse working = CalendarResponse.builder()
                        .events(new ArrayList<>())
                        .status(CalendarStatus.WORKING)
                        .date(thisDate)
                        .build();
                expected.add(working);
            }
        }

        getCalendarCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(eventRepository, Mockito.times(1)).findByDateBetweenOrderByDateAsc(startOfThisMonth, StartOfNextMonth);

    }
}
