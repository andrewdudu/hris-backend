package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.AddAnnouncementCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AnnouncementRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
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
public class AddAnnouncementCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public AddAnnouncementCommand addAnnouncementCommand(){
            return new AddAnnouncementCommandImpl();
        }
    }

    @Autowired
    private AddAnnouncementCommand addAnnouncementCommand;

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

        String title = "Announcement 1";
        String notes = "announcement";
        AnnouncementRequest request = AnnouncementRequest.builder()
                .title(title)
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-30 10:00:00");
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-30");
        Mockito.when(eventRepository.findByTitleAndDate(title, startOfDate))
                .thenReturn(Mono.empty());

        String id = "UUID";
        Mockito.when(uuidUtil.getNewID()).thenReturn(id);
        Event event = Event.builder()
                .title(title)
                .date(startOfDate)
                .status(CalendarStatus.ANNOUNCEMENT)
                .description(notes)
                .build();
        event.setId(id);
        event.setCreatedDate(currentDate);
        event.setCreatedBy(user.getUsername());

        Mockito.when(eventRepository.save(event)).thenReturn(Mono.just(event));

        AnnouncementResponse expected = AnnouncementResponse.builder()
                .title(title)
                .notes(notes)
                .build();

        addAnnouncementCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(eventRepository, Mockito.times(1)).findByTitleAndDate(title, startOfDate);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(eventRepository, Mockito.times(1)).save(event);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
    }

    @Test(expected = Exception.class)
    public void test_execute_eventAlreadyExists() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        String title = "Announcement 1";
        String notes = "announcement";
        AnnouncementRequest request = AnnouncementRequest.builder()
                .title(title)
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        String id = "UUID";
        Mockito.when(uuidUtil.getNewID()).thenReturn(id);

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-30 10:00:00");

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-30");

        Event event = Event.builder()
                .title(title)
                .date(startOfDate)
                .status(CalendarStatus.ANNOUNCEMENT)
                .description(notes)
                .build();
        event.setId(id);
        event.setCreatedDate(currentDate);
        event.setCreatedBy(user.getUsername());

        event.setCreatedBy(user.getUsername());
        Mockito.when(eventRepository.findByTitleAndDate(title, startOfDate))
                .thenReturn(Mono.just(event));

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        addAnnouncementCommand.execute(request).subscribe();

        Mockito.verify(eventRepository, Mockito.times(1)).findByTitleAndDate(title, startOfDate);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(eventRepository, Mockito.times(0)).save(event);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
    }
}
