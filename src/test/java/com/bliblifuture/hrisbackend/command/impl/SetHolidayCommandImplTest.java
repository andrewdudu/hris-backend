package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.SetHolidayCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class SetHolidayCommandImplTest {

    @TestConfiguration
    static class command{
        @Bean
        public SetHolidayCommand setHolidayCommand(){
            return new SetHolidayCommandImpl();
        }
    }

    @Autowired
    private SetHolidayCommand setHolidayCommand;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-3");
        String title = "Work day";
        String desc = "Work";
        SetHolidayRequest request = SetHolidayRequest.builder()
                .name(title)
                .notes(desc)
                .status(CalendarStatus.WORKING)
                .date(date)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(eventRepository.findByTitleAndDate(title, date))
                .thenReturn(Mono.empty());

        String id = "UUID";
        Mockito.when(uuidUtil.getNewID()).thenReturn(id);

        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-1");
        Mockito.when(dateUtil.getNewDate()).thenReturn(date2);

        Event event = Event.builder()
                .title(title)
                .date(date)
                .status(CalendarStatus.WORKING)
                .description(desc)
                .build();
        event.setId(id);
        event.setCreatedDate(date2);
        event.setCreatedBy(user.getUsername());

        Mockito.when(eventRepository.save(event)).thenReturn(Mono.just(event));

        EventDetailResponse expected = EventDetailResponse.builder()
                .name(title)
                .notes(desc)
                .status(CalendarStatus.WORKING)
                .build();

        setHolidayCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(eventRepository, Mockito.times(1)).findByTitleAndDate(title, date);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(eventRepository, Mockito.times(1)).save(event);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();

    }
}
