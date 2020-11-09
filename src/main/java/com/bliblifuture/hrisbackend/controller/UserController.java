package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetCurrentUserCommand;
import com.bliblifuture.hrisbackend.command.GetLeavesQuotaCommand;
import com.bliblifuture.hrisbackend.command.GetLeavesReportCommand;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.model.response.LeavesReportResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
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

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/current-user")
    public Mono<Response<UserResponse>> getUser(Principal principal){
        return commandExecutor.execute(GetCurrentUserCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/{id}/leave-quotas")
    public Mono<Response<List<LeaveResponse>>> getLeavesQuota(@PathVariable("id") String id){
        return commandExecutor.execute(GetLeavesQuotaCommand.class, id)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/{id}/profile")
    public Mono<Response<LeavesReportResponse>> getLeavesReport(@PathVariable("id") String id){
        return commandExecutor.execute(GetLeavesReportCommand.class, id)
                .map(ResponseHelper::ok)
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
