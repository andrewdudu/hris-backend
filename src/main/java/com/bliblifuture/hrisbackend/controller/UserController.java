package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/user")
public class UserController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping
    public Mono<Response<String>> testing(){
        return Mono.just(ResponseHelper.ok("halo"));
    }

    @PostMapping("/current-user")
    public Mono<Response<UserResponse>> getUser(LoginRequest request, ServerWebExchange swe){
        return commandExecutor.execute(LoginCommand.class, request)
                .map(loginResponse -> {
                    swe.getResponse()
                            .addCookie(ResponseCookie
                                    .from("userToken", loginResponse.getAccessToken())
                                    .maxAge(7*3600)
                                    .secure(true)
                                    .httpOnly(true)
                                    .build());
                    return ResponseHelper.ok(loginResponse.getUserResponse());
                })
                .subscribeOn(Schedulers.elastic());
    }

//    @GetMapping("/test")
//    public Mono<Response<String>> cookieTest(ServerWebExchange swe){
//        UserEntity userEntity = UserEntity.builder().username("test").password("test")
//                .roles(Arrays.asList("ADMIN", "EMPLOYEE"))
//                .build();
//        ResponseCookie cookie = ResponseCookie
//                .from("userToken",jwtTokenUtil.generateToken(userEntity))
//                .maxAge(5*3600)
//                .secure(true)
//                .httpOnly(true)
//                .build();
//
//        swe.getResponse().addCookie(cookie);
//        return Mono.just(ResponseHelper.ok("test"));
//    }

//    @GetMapping("/get-test")
//    public Mono<Response<String>> getCookieTest(ServerWebExchange swe){
////        String token = swe.getRequest().getCookies().getFirst("userToken").getValue();
//        String t = swe.getRequest().getHeaders().getFirst(HttpHeaders.SET_COOKIE);
//
//        return Mono.just(ResponseHelper.ok(t));
//    }
}
