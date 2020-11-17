package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.request.RequestAttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.RequestAttendanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/request")
public class RequestController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/attendances")
    public Mono<Response<RequestAttendanceResponse>> requestAttendances(@RequestBody RequestAttendanceRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestAttendanceCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/attendances")
    public Mono<Response<List<AnnouncementResponse>>> requestLeave(@RequestParam("page") int page, @RequestParam("size") int size){
        PagingRequest request = new PagingRequest(page, size);
        return commandExecutor.execute(GetAnnouncementCommand.class, request)
                .map(pagingResponse -> {
                    Response<List<AnnouncementResponse>> response = ResponseHelper.ok(pagingResponse.getData());
                    response.setPaging(pagingResponse.getPaging());
                    return response;
                })
                .subscribeOn(Schedulers.elastic());
    }

}
