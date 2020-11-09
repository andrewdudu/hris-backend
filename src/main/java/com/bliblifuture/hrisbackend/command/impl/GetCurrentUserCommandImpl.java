package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCurrentUserCommand;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
public class GetCurrentUserCommandImpl implements GetCurrentUserCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Override
    public Mono<UserResponse> execute(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::getResponse);
    }

    private Mono<UserResponse> getResponse(User user) {
        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .build();

        return getEmployeeData(userResponse, user.getEmployeeId());
    }

    private Mono<UserResponse> getEmployeeData(UserResponse response, String employeeId)
    {
        return employeeRepository.findById(employeeId)
                .flatMap(employee -> setDepartmentById(response, employee.getDepId())
                        .flatMap(res -> setTotalRemainingLeaves(res, employee.getId()))
                );
    }

    private Mono<UserResponse> setDepartmentById(UserResponse response, String depId){
        return Mono.from(departmentRepository.findById(depId))
                .map(department -> {
                    response.setDepartment(department.getName());
                    return response;
                });
    }

    private Mono<UserResponse> setTotalRemainingLeaves(UserResponse response, String username) {
        return leaveRepository.findByEmployeeIdAndExpDateAfter(username, new Date())
                .collectList()
                .map(leaves -> {
                    response.setLeaveResponse(LeaveResponse.builder()
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
