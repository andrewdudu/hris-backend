package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.constant.RequestType;
import com.bliblifuture.hrisbackend.constant.SpecialRequestType;
import com.bliblifuture.hrisbackend.model.entity.RequestLeave;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;
import com.bliblifuture.hrisbackend.model.response.LeavesReportResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController extends WebMvcProperties {

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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
    @GetMapping("/{id}/profile")
    public Mono<Response<LeavesReportResponse>> getLeavesReport(@PathVariable("id") String id){
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
    public Mono<Response<List<SpecialRequestType>>> getAvailableSpecialRequests(Principal principal){
        return commandExecutor.execute(GetAvailableSpecialRequestsCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/current-user/attendance-summary")
    public Mono<Response<List<AttendanceSummaryResponse>>> getAttendanceSummmary(Principal principal){
        return commandExecutor.execute(GetAttendanceSummaryCommand.class, principal.getName())
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

    @GetMapping("/get-test")
    public Mono<Response<RequestLeave>> getCookieTest(ServerWebExchange swe) throws ParseException {
//        String token = swe.getRequest().getCookies().getFirst("userToken").getValue();
//        String t = swe.getRequest().getHeaders().getFirst(HttpHeaders.SET_COOKIE);

//        Date date = new SimpleDateFormat("dd/MM/yy").parse("1/1/2020");
//
//        System.out.println(date);
//
//        String tanggal = String.valueOf(date.getDate());
//        String bulan = String.valueOf(date.getMonth() + 1);
//        String tahun = String.valueOf(date.getYear() + 1900);
//
//        return Mono.just(ResponseHelper.ok(tanggal + " " + bulan + " " + tahun));

        RequestLeave summary = RequestLeave.builder().build();
        System.out.println(summary.getDates());

        return Mono.just(ResponseHelper.ok(summary));
    }
}
