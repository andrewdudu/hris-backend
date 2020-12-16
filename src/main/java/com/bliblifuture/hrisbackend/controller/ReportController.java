package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetLeavesDetailResponseCommand;
import com.bliblifuture.hrisbackend.model.request.GetLeavesDetailRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/report/leaves")
public class ReportController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Mono<Response<List<LeaveDetailResponse>>> getEmployeeDashboard(
            @RequestParam(value = "department", required = false) String department, @RequestParam(value = "month", required = false, defaultValue = "0") String month)
    {
        GetLeavesDetailRequest request =  GetLeavesDetailRequest.builder()
                .department(department)
                .month(Integer.parseInt(month))
                .build();
        return commandExecutor.execute(GetLeavesDetailResponseCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }
}
