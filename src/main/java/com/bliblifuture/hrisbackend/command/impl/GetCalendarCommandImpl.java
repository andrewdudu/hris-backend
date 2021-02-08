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

        Date startOfThisMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(thisYear + "-" + thisMonth + "-1 00:00:00");
        Date startOfNextMonth;
        if (thisMonth == 12){
            startOfNextMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse((thisYear+1) + "-1-1 00:00:00");
        }
        else{
            startOfNextMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse(thisYear + "-" + (thisMonth +1) + "-1 00:00:00");
        }

        Date endOfThisMonth = new Date(startOfNextMonth.getTime() - TimeUnit.SECONDS.toMillis(1));

        return eventRepository.findByDateBetweenOrderByDateAsc(startOfThisMonth, startOfNextMonth)
                .switchIfEmpty(Flux.empty())
                .filter(event -> !event.getStatus().equals(CalendarStatus.ANNOUNCEMENT))
                .collectList()
                .map(events -> createResponse(events, endOfThisMonth));
    }

    @SneakyThrows
    private List<CalendarResponse> createResponse(List<Event> events, Date endOfThisMonth) {
        List<CalendarResponse> responses = new ArrayList<>();

        int month = endOfThisMonth.getMonth()+1;
        int year = endOfThisMonth.getYear()+1900;

        for (int i = 1; i <= endOfThisMonth.getDate(); i++) {
            Date thisDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(year + "-" + month + "-" + i);
            CalendarResponse response = CalendarResponse.builder()
                    .date(thisDate)
                    .events(new ArrayList<>())
                    .build();
            if (thisDate.getDay() == 0 || thisDate.getDay() == 6){
                response.setStatus(CalendarStatus.HOLIDAY);
            }
            else{
                response.setStatus(CalendarStatus.WORKING);
            }
            responses.add(response);
        }

        if (!events.isEmpty()){
            for (Event event : events) {
                CalendarResponse thisDateData = responses.get(event.getDate().getDate() - 1);
                EventDetailResponse detail = EventDetailResponse.builder().name(event.getTitle()).build();
                thisDateData.getEvents().add(detail);
                if (thisDateData.getStatus().equals(CalendarStatus.WORKING)) {
                    thisDateData.setStatus(event.getStatus());
                }
            }
        }
        return responses;
    }

}
