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

        return eventRepository.findByDateBetweenOrderByDateAsc(start, before)
                .switchIfEmpty(Flux.empty())
                .collectList()
                .map(this::createResponse);
    }

    private List<CalendarResponse> createResponse(List<Event> events) {
        List<CalendarResponse> responses = new ArrayList<>();
        if (!events.isEmpty()){
            CalendarResponse firstData = CalendarResponse.builder()
                    .date(events.get(0).getDate())
                    .status(events.get(0).getStatus())
                    .events(new ArrayList<>())
                    .build();
            firstData.getEvents().add(EventDetailResponse.builder()
                    .name(events.get(0).getTitle())
                    .build()
            );
            responses.add(firstData);
            for (int i = 1; i < events.size(); i++) {
                CalendarResponse currentLastData = responses.get(responses.size()-1);
                Event event = events.get(i);
                if (currentLastData.getDate().equals(event.getDate())){
                    EventDetailResponse detail = EventDetailResponse.builder().name(event.getTitle()).build();
                    currentLastData.getEvents().add(detail);
                    if (currentLastData.getStatus().equals(CalendarStatus.WORKING) && event.getStatus().equals(CalendarStatus.HOLIDAY)){
                        currentLastData.setStatus(CalendarStatus.HOLIDAY);
                    }
                }
                else{
                    CalendarResponse response = CalendarResponse.builder()
                            .date(event.getDate())
                            .status(event.getStatus())
                            .events(new ArrayList<>())
                            .build();
                    response.getEvents().add(EventDetailResponse.builder()
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
