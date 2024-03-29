package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.request.LeaveQuotaFormRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveQuotaFormResponse;
import com.bliblifuture.hrisbackend.model.response.UserReportResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveReportResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/current-user")
    public Mono<Response<UserResponse>> getUser(Principal principal){
        return commandExecutor.execute(GetCurrentUserCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}/leave-quotas")
    public Mono<Response<List<LeaveResponse>>> getLeavesQuota(@PathVariable("id") String id){
        return commandExecutor.execute(GetLeavesQuotaCommand.class, id)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/leave-quotas")
    public Mono<Response<LeaveQuotaFormResponse>> getLeavesQuotaForm(@RequestParam String code,
                                                                     Principal principal){
        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder()
                .code(code)
                .build();
        request.setRequester(principal.getName());
        return commandExecutor.execute(GetLeaveQuotaFormCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}/profile")
    public Mono<Response<LeaveReportResponse>> getLeavesReport(@PathVariable("id") String id){
        return commandExecutor.execute(GetLeavesReportCommand.class, id)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/current-user/available-requests")
    public Mono<Response<List<RequestType>>> getAvailableRequests(Principal principal){
        return commandExecutor.execute(GetAvailableRequestsCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/current-user/available-special-requests")
    public Mono<Response<List<SpecialLeaveType>>> getAvailableSpecialRequests(Principal principal){
        return commandExecutor.execute(GetAvailableSpecialRequestsCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/current-user/attendance-summary")
    public Mono<Response<UserReportResponse>> getAttendanceSummmary(Principal principal){
        return commandExecutor.execute(GetAttendanceSummaryCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }
}
