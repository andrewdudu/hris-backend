package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.LoginResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
public class LoginCommandImpl implements LoginCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public Mono<LoginResponse> execute(LoginRequest request) {
        return Mono.fromCallable(request::getUsername)
                .flatMap(username -> userRepository.findByUsername(username)
                        .doOnSuccess(this::checkNull)
                        .flatMap(user -> authenticateAndGetResponse(user, request))
                );
    }

    private void checkNull(User user) {
        throw new SecurityException("DOES_NOT_MATCH");
    }

    private Mono<LoginResponse> authenticateAndGetResponse(User user, LoginRequest request) {
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())){
            String token = jwtTokenUtil.generateToken(user);

            UserResponse userResponse = UserResponse.builder()
                    .roles(user.getRoles())
                    .username(user.getUsername())
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .accessToken(token).userResponse(userResponse).build();

            return getEmployeeData(response, user.getEmployeeId());
        }
        else{
            throw new SecurityException("DOES_NOT_MATCH");
        }
    }

    private Mono<LoginResponse> getEmployeeData(LoginResponse response, String employeeId)
    {
        return employeeRepository.findById(employeeId)
                .flatMap(employee -> setDepartmentById(response, employee.getDepId())
                        .flatMap(res -> setTotalRemainingLeaves(res, employee.getId()))
                );
    }

    private Mono<LoginResponse> setDepartmentById(LoginResponse response, String depId){
        return Mono.from(departmentRepository.findById(depId))
                .map(department -> {
                    response.getUserResponse().setDepartment(department.getName());
                    return response;
                });
    }

    private Mono<LoginResponse> setTotalRemainingLeaves(LoginResponse response, String username) {
        return leaveRepository.findByEmployeeIdAndExpDateAfter(username, new Date())
                .collectList()
                .map(leaves -> {
                    response.getUserResponse()
                            .setLeaveResponse(LeaveResponse.builder()
                                    .remaining(countLeaves(leaves))
                                    .build());

                    return response;
                });
    }

    private int countLeaves(List<Leave> leaves) {
        int total = 0;
        for (Leave l : leaves) {
            total += l.getRemaining();
        }
        return total;
    }

}
