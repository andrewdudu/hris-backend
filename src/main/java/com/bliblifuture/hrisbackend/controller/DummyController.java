package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.config.PassEncoder;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@RestController
@RequestMapping("/api/dummy")
public class DummyController {

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PassEncoder passEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @PostMapping("/create-user")
    public Mono<Response<User>> createUser(@RequestBody User user){
        user.setPassword(passEncoder.encode(user.getPassword()));
        user.setId("USER-" + user.getEmployeeId());
        return userRepository.save(user)
                .map(res -> ResponseHelper.ok(res));
    }

    @PostMapping("/create-employee")
    @SneakyThrows
    public Mono<Response<Employee>> createEmployee(@RequestBody Employee employee){
        employee.setJoinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-06-25"));
        return employeeRepository.save(employee)
                .map(res -> ResponseHelper.ok(res));
    }

    @PostMapping("/create-office")
    @SneakyThrows
    public Mono<Response<Office>> createOfficce(@RequestBody Office office){
        return officeRepository.save(office)
                .map(res -> ResponseHelper.ok(res));
    }

    @PostMapping("/create-dep")
    @SneakyThrows
    public Mono<Response<Department>> createDep(@RequestBody Department department){
        return departmentRepository.save(department)
                .map(res -> ResponseHelper.ok(res));
    }

    @GetMapping("/test")
    public Mono<Response<String>> cookieTest(ServerWebExchange swe){
        User userEntity = User.builder().username("test").password("test")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .build();
        ResponseCookie cookie = ResponseCookie
                .from("userToken",jwtTokenUtil.generateToken(userEntity))
                .maxAge(5*3600)
                .secure(true)
                .httpOnly(true)
                .build();

        swe.getResponse().addCookie(cookie);
        return Mono.just(ResponseHelper.ok("test"));
    }

    @GetMapping("/get-test")
    public Mono<Response<Request>> getCookieTest(ServerWebExchange swe) throws ParseException {
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

        Request summary = Request.builder().build();
        System.out.println(summary.getDates());

        return Mono.just(ResponseHelper.ok(summary));
    }
}
