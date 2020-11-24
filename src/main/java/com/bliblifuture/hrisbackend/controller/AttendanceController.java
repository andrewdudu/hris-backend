package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.command.GetAttendancesCommand;
import com.bliblifuture.hrisbackend.model.request.AttendanceListRequest;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/_clock-in")
    public Mono<Response<AttendanceResponse>> clockIn(@RequestBody ClockInClockOutRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(ClockInCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/_clock-out")
    public Mono<Response<AttendanceResponse>> clockOut(@RequestBody ClockInClockOutRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(ClockOutCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public Mono<Response<List<AttendanceResponse>>> getAttendances(@RequestParam Date startDate, @RequestParam Date endDate, Principal principal){
        AttendanceListRequest request = AttendanceListRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .username(principal.getName())
                .build();
        return commandExecutor.execute(GetAttendancesCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }


}
