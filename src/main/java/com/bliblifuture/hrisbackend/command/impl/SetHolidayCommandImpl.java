package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.SetHolidayCommand;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SetHolidayCommandImpl implements SetHolidayCommand {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<EventDetailResponse> execute(SetHolidayRequest request) {
        if (request.getStatus() == null){
            String msg = "status=STATUS_IS_NULL";
            throw new IllegalArgumentException(msg);
        }
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> createEvent(request, user))
                .flatMap(event -> eventRepository.save(event))
                .map(this::createResponse);
    }

    private EventDetailResponse createResponse(Event event) {
        return EventDetailResponse.builder()
                .name(event.getTitle())
                .notes(event.getDescription())
                .status(event.getStatus())
                .build();
    }

    @SneakyThrows
    private Mono<Event> createEvent(SetHolidayRequest request, User user) {
        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(request.getDate());
        Event event = Event.builder()
                        .date(date)
                        .description(request.getNotes())
                        .status(request.getStatus())
                        .title(request.getName())
                        .build();
        event.setId(uuidUtil.getNewID());
        event.setCreatedBy(user.getUsername());
        event.setCreatedDate(dateUtil.getNewDate());
        return eventRepository.findByTitleAndDate(request.getName(), date)
                .doOnNext(this::checkIfExists)
                .switchIfEmpty(Mono.just(event));
    }

    private void checkIfExists(Event event) {
        if (event != null){
            String msg = "message=EVENT_ALREADY_EXISTS";
            throw new IllegalArgumentException(msg);
        }
    }

}
