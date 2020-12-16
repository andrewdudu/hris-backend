package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/auth")
public class AuthController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @PostMapping("/login")
    public Mono<Response<UserResponse>> login(@RequestBody LoginRequest request, ServerWebExchange swe){
        return commandExecutor.execute(LoginCommand.class, request)
                .map(loginResponse -> {
                    swe.getResponse()
                            .addCookie(ResponseCookie
                                    .from("userToken", loginResponse.getAccessToken())
                                    .maxAge(14*3600)
                                    .secure(false)
                                    .httpOnly(true)
                                    .path("/")
                                    .build());
                    return ResponseHelper.ok(loginResponse.getUserResponse());
                })
                .subscribeOn(Schedulers.elastic());
    }

    @PostMapping("/logout")
    public Mono<Response<String>> login(ServerWebExchange swe){
        swe.getResponse()
                .addCookie(ResponseCookie
                        .from("userToken", "")
                        .maxAge(0)
                        .secure(false)
                        .httpOnly(true)
                        .path("/")
                        .build());
        return Mono.just(ResponseHelper.ok("LOGGED_OUT"));
    }

}
