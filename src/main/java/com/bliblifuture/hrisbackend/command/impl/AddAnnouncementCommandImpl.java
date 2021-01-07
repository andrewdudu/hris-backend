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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AddAnnouncementCommandImpl implements AddAnnouncementCommand {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<AnnouncementResponse> execute(AnnouncementRequest request) {
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> createAnnouncement(request, user))
                .flatMap(event -> eventRepository.save(event))
                .map(this::createResponse);
    }

    private AnnouncementResponse createResponse(Event event) {
        return AnnouncementResponse.builder()
                .title(event.getTitle())
                .notes(event.getDescription())
                .build();
    }

    @SneakyThrows
    private Mono<Event> createAnnouncement(AnnouncementRequest request, User user) {
        Date currentDate = dateUtil.getNewDate();

        String startDate = (currentDate.getYear()+1900) + "-" + (currentDate.getMonth()+1) + "-"
                + currentDate.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(startDate + startTime);

        Event event = Event.builder()
                .date(startOfDate)
                .description(request.getNotes())
                .title(request.getTitle())
                .status(CalendarStatus.ANNOUNCEMENT)
                .build();
        event.setId(uuidUtil.getNewID());
        event.setCreatedBy(user.getUsername());
        event.setCreatedDate(currentDate);
        return eventRepository.findByTitleAndDate(request.getTitle(), startOfDate)
                .doOnNext(this::checkIfExists)
                .switchIfEmpty(Mono.just(event));
    }

    private void checkIfExists(Event event) {
        if (event != null){
            String msg = "message=ANNOUNCEMENT_ALREADY_EXISTS";
            throw new IllegalArgumentException(msg);
        }
    }

}
