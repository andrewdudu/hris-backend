package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCalendarCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GetCalendarCommandImpl implements GetCalendarCommand {

    @Autowired
    private EventRepository eventRepository;

    @SneakyThrows
    @Override
    public Mono<List<CalendarResponse>> execute(GetCalendarRequest request) {
        int thisYear = request.getYear();
        int thisMonth = request.getMonth();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(thisYear + "-" + thisMonth + "-1 00:00:00");
        Date before;
        if (thisMonth == 12){
            before = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse((thisYear+1) + "-1-1 00:00:00");
        }
        else{
            before = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse(thisYear + "-" + (thisMonth +1) + "-1 00:00:00");
        }

        Date lastDateThisMonth = new Date(before.getTime() - TimeUnit.SECONDS.toMillis(1));

        return eventRepository.findByDateBetweenOrderByDateAsc(start, before)
                .switchIfEmpty(Flux.empty())
                .collectList()
                .map(events -> createResponse(events, lastDateThisMonth));
    }

    @SneakyThrows
    private List<CalendarResponse> createResponse(List<Event> events, Date lastDate) {
        List<CalendarResponse> responses = new ArrayList<>();

        int month = lastDate.getMonth()+1;
        int year = lastDate.getYear()+1900;

        for (int i = 1; i <= lastDate.getDate(); i++) {
            Date thisDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(year + "-" + month + "-" + i);
            CalendarResponse response = CalendarResponse.builder()
                    .date(thisDate)
                    .events(new ArrayList<>())
                    .build();
            if (thisDate.getDay() == 0){
                response.setStatus(CalendarStatus.HOLIDAY);
            }
            else{
                response.setStatus(CalendarStatus.WORKING);
            }
            responses.add(response);
        }

        if (!events.isEmpty()){
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                CalendarResponse thisDateData = responses.get(event.getDate().getDate()-1);
                if (thisDateData.getDate().equals(event.getDate())){
                    EventDetailResponse detail = EventDetailResponse.builder().name(event.getTitle()).build();
                    thisDateData.getEvents().add(detail);
                    if (event.getStatus().equals(CalendarStatus.WORKING)){
                        thisDateData.setStatus(CalendarStatus.HOLIDAY);
                    }
                }
                else{
                    CalendarResponse response = CalendarResponse.builder()
                            .date(event.getDate())
                            .status(event.getStatus())
                            .events(new ArrayList<>())
                            .build();
                    responses.get(event.getDate().getDate()-1).getEvents().add(EventDetailResponse.builder()
                            .name(events.get(i).getTitle())
                            .build()
                    );
                    responses.add(response);
                }
            }
        }
        return responses;
    }

}
