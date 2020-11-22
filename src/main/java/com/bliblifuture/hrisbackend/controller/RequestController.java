package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.command.RequestExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequestData;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.AttendanceRequestResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;

@RestController
@RequestMapping("/api/request")
public class RequestController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/attendances")
    public Mono<Response<AttendanceRequestResponse>> requestAttendances(@RequestBody AttendanceRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestAttendanceCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/leaves")
    public Mono<Response<LeaveRequestResponse>> requestLeave(@RequestBody LeaveRequestData requestData, Principal principal){
        requestData.setRequester(principal.getName());
        return commandExecutor.execute(RequestLeaveCommand.class, requestData)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> getExtendLeaveData(Principal principal){
        return commandExecutor.execute(GetExtendLeaveDataCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> requestExtendLeave(@RequestBody LeaveRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestExtendLeaveDataCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

}
