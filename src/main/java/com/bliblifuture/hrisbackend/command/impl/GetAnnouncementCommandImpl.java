package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.repository.EventRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class GetAnnouncementCommandImpl implements GetAnnouncementCommand {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<PagingResponse<AnnouncementResponse>> execute(PagingRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        PagingResponse<AnnouncementResponse> response = new PagingResponse<>();

        Date currentDate = dateUtil.getNewDate();
        int year = currentDate.getYear() + 1899;

        Date lastTimeOfLastYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(year + "-12-31" + " 23:59:59");

        return eventRepository.findAllByDateAfterOrderByDateAsc(lastTimeOfLastYear, pageable)
                .switchIfEmpty(Flux.empty())
                .map(events -> events.createResponse(events, new AnnouncementResponse()))
                .collectList()
                .flatMap(announcementResponseList -> {
                    response.setData(announcementResponseList);
                    return eventRepository.countAllByDateAfter(lastTimeOfLastYear);
                })
                .map(total -> response.setPagingDetail(request, Math.toIntExact(total)));
    }

}
