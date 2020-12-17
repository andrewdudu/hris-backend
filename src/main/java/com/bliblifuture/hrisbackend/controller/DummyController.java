package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.config.PassEncoder;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

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
    private LeaveRepository leaveRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeElasticsearchRepository employeeElasticsearchRepository;

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

    @PostMapping("/create-leave")
    @SneakyThrows
    public Mono<Response<Leave>> createLeave(@RequestBody Leave leave){
        leave.setId(UUID.randomUUID().toString());
        leave.setExpDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-1-1"));
        return leaveRepository.save(leave)
                .map(res -> ResponseHelper.ok(res));
    }

    @PostMapping("/create-event")
    @SneakyThrows
    public Mono<Response<Event>> createEvent(@RequestBody Event event){
        event.setId(UUID.randomUUID().toString());
        event.setDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-2"));
        return eventRepository.save(event)
                .map(res -> ResponseHelper.ok(res));
    }

    @PostMapping("/save-elastic")
    public Mono<Response<String>> saveEmployeeElastic(){
        return employeeRepository.findAll()
                .flatMap(employee -> employeeElasticsearchRepository.findById(employee.getId())
                        .switchIfEmpty(Mono.just(EmployeeIndex.builder().build()))
                        .flatMap(employeeIndex -> saveNewData(employee, employeeIndex))
                )
                .collectList()
                .map(res -> ResponseHelper.ok("OK"));
    }

    private Mono<EmployeeIndex> saveNewData(Employee employee, EmployeeIndex employeeIndex) {
        if (employeeIndex.getId() == null || employeeIndex.getId().isEmpty()){
            employeeIndex.setId(employee.getId());
            employeeIndex.setName(employee.getName());
            employeeIndex.setDepartmentId(employee.getDepId().replace("-",""));

            return employeeElasticsearchRepository.save(employeeIndex);
        }
        return Mono.empty();
    }

    @GetMapping("/test")
    public Mono<Response<String>> cookieTest(ServerWebExchange swe) throws ParseException {
//        User userEntity = User.builder().username("test").password("test")
//                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
//                .build();
//        ResponseCookie cookie = ResponseCookie
//                .from("userToken",jwtTokenUtil.generateToken(userEntity))
//                .maxAge(5*3600)
//                .secure(true)
//                .httpOnly(true)
//                .build();

//        swe.getResponse().addCookie(cookie);
        System.out.println(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-3").getTime());
        System.out.println(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-4").getTime());
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
