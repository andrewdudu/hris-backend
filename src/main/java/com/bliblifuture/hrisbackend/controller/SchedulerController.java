package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.AutoClockoutCommand;
import com.bliblifuture.hrisbackend.command.UpdateLeaveQuotaCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update-leave")
    public Mono<Response<String>> updateLeaveQuota(){
        return commandExecutor.execute(UpdateLeaveQuotaCommand.class, "")
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto-clockout")
    public Mono<Response<String>> autoClockout(){
        return commandExecutor.execute(AutoClockoutCommand.class, "")
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }
}
