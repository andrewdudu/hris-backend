package com.bliblifuture.hrisbackend.command.impl;

import com.blibli.oss.common.paging.Paging;
import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetAnnouncementCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetAnnouncementCommand getAnnouncementCommand(){
            return new GetAnnouncementCommandImpl();
        }
    }

    @Autowired
    private GetAnnouncementCommand getAnnouncementCommand;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        int page = 0;
        int size = 10;
        PagingRequest request = new PagingRequest(page, size);

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 08:00:00");
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse("2020-12-10");

        Event event1 = Event.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12"))
                .description("Flash Sale")
                .status(CalendarStatus.HOLIDAY)
                .title("Holiday")
                .build();
        Event event2 = Event.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14"))
                .description("CEO's Bday")
                .status(CalendarStatus.WORKING)
                .title("CEO's Birthday")
                .build();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Mockito.when(eventRepository.findAllByDateAfterOrderByDateAsc(startOfDate, pageable))
                .thenReturn(Flux.just(event1, event2));

        AnnouncementResponse announcementResponse1 = AnnouncementResponse.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12"))
                .description("Flash Sale")
                .status(CalendarStatus.HOLIDAY)
                .title("Holiday")
                .build();
        AnnouncementResponse announcementResponse2 = AnnouncementResponse.builder()
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14"))
                .description("CEO's Bday")
                .status(CalendarStatus.WORKING)
                .title("CEO's Birthday")
                .build();

        List<AnnouncementResponse> data = Arrays.asList(announcementResponse1, announcementResponse2);

        Mockito.when(eventRepository.countAllByDateAfter(startOfDate))
                .thenReturn(Mono.just(2L));

        Paging paging = Paging.builder()
                .page(request.getPage())
                .itemPerPage(request.getSize())
                .totalItem(data.size())
                .totalPage(1)
                .build();

        PagingResponse<AnnouncementResponse> expected = new PagingResponse<>();
        expected.setData(data);
        expected.setPaging(paging);

        getAnnouncementCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getPaging(), response.getPaging());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(eventRepository, Mockito.times(1))
                .findAllByDateAfterOrderByDateAsc(startOfDate, pageable);
        Mockito.verify(eventRepository, Mockito.times(1))
                .countAllByDateAfter(startOfDate);
    }
}
