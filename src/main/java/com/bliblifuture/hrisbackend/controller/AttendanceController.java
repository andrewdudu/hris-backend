package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.ClockInClockOutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/_clock-in")
    public Mono<Response<ClockInClockOutResponse>> clockIn(@RequestBody ClockInClockOutRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(ClockInCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/_clock-out")
    public Mono<Response<ClockInClockOutResponse>> clockOut(@RequestBody ClockInClockOutRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(ClockOutCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }


}
