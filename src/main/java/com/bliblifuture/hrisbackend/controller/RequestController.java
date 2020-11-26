package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequestData;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.AttendanceRequestResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping
public class RequestController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/attendances")
    public Mono<Response<AttendanceRequestResponse>> requestAttendances(@RequestBody AttendanceRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestAttendanceCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/leaves")
    public Mono<Response<LeaveRequestResponse>> requestLeave(@RequestBody LeaveRequestData requestData, Principal principal){
        requestData.setRequester(principal.getName());
        return commandExecutor.execute(RequestLeaveCommand.class, requestData)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/api/request/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> getExtendLeaveData(Principal principal){
        return commandExecutor.execute(GetExtendLeaveDataCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> requestExtendLeave(@RequestBody LeaveRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestExtendLeaveCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/requests")
    public Mono<Response<List<RequestResponse>>> getIncomingRequests(@RequestParam("type") String type){
        return commandExecutor.execute(GetIncomingRequestCommand.class, type)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/requests/{id}/_approve")
    public Mono<Response<RequestResponse>> approveRequest(@PathVariable("id") String id, Principal principal){
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());
        return commandExecutor.execute(ApproveRequestCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/requests/{id}/_reject")
    public Mono<Response<RequestResponse>> rejectRequest(@PathVariable("id") String id, Principal principal){
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());
        return commandExecutor.execute(RejectRequestCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

}
