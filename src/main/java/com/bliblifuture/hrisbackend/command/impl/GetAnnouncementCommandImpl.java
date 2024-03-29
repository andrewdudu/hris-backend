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

        String dateString = (currentDate.getYear() + 1900) + "-" + (currentDate.getMonth() + 1) + "-" + (currentDate.getDate()-1);

        String endTime = " 23:59:00";
        Date endOfYesterday = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + endTime);

        return eventRepository.findAllByDateAfterOrderByDateAsc(endOfYesterday, pageable)
                .switchIfEmpty(Flux.empty())
                .map(events -> events.createResponse(
                        events, AnnouncementResponse.builder().build()
                ))
                .collectList()
                .flatMap(announcementResponseList -> {
                    response.setData(announcementResponseList);
                    return eventRepository.countAllByDateAfter(endOfYesterday);
                })
                .map(total -> response.setPagingDetail(request, Math.toIntExact(total)));
    }

}
