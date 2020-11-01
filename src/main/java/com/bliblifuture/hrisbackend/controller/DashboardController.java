package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/summary")
    public Mono<Response<DashboardResponse>> getAdminDashboard(Principal principal){
        return commandExecutor.execute(GetDashboardSummaryCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }
}
