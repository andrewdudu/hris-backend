package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCurrentUserCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.UserResponseHelper;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    @Autowired
    private UserResponseHelper userResponseHelper;

    @Override
    public Mono<UserResponse> execute(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> userResponseHelper.getUserResponse(user));
    }

}
