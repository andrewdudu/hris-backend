package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class GetAnnouncementCommandImpl implements GetAnnouncementCommand {

    @Autowired
    private EventRepository eventRepository;

    @SneakyThrows
    @Override
    public Mono<PagingResponse<AnnouncementResponse>> execute(PagingRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        PagingResponse<AnnouncementResponse> response = new PagingResponse<>();

        Date currentDate = new Date();
        int year = currentDate.getYear() + 1899;

        Date lastTimeOfLastYear = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .parse("31/12/" + year + " 23:59:59");

        return eventRepository.findAllByDateAfterOrderByDateAsc(lastTimeOfLastYear, pageable)
                .map(events -> events.createResponse(events, new AnnouncementResponse()))
                .collectList()
                .flatMap(announcementResponseList -> {
                    response.setData(announcementResponseList);
                    return eventRepository.countAllByDateAfter(lastTimeOfLastYear);
                })
                .map(total -> response.getPagingResponse(request, Math.toIntExact(total)));
    }

}
