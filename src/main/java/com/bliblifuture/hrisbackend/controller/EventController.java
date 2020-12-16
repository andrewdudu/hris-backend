package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.command.GetCalendarCommand;
import com.bliblifuture.hrisbackend.command.SetHolidayCommand;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/announcements")
    public Mono<Response<List<AnnouncementResponse>>> getAnnouncements(@RequestParam("page") int page, @RequestParam("size") int size){
        PagingRequest request = new PagingRequest(page, size);
        return commandExecutor.execute(GetAnnouncementCommand.class, request)
                .map(pagingResponse -> {
                    Response<List<AnnouncementResponse>> response = ResponseHelper.ok(pagingResponse.getData());
                    response.setPaging(pagingResponse.getPaging());
                    return response;
                })
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/calendar/days")
    public Mono<Response<List<CalendarResponse>>> getCalendar(@RequestParam("month") int month, @RequestParam("year") int year){
        GetCalendarRequest request = GetCalendarRequest.builder()
                .month(month)
                .year(year)
                .build();
        return commandExecutor.execute(GetCalendarCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/calendar/days/{date}/events")
    public Mono<Response<EventDetailResponse>> setHoliday(@PathVariable("date") String date, @RequestBody SetHolidayRequest request, Principal principal){
        request.setDate(date);
        request.setRequester(principal.getName());
        return commandExecutor.execute(SetHolidayCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

}
