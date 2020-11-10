package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableSpecialRequestsCommand;
import com.bliblifuture.hrisbackend.constant.SpecialRequestType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAvailableSpecialRequestsCommandImpl implements GetAvailableSpecialRequestsCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Override
    public Mono<List<SpecialRequestType>> execute(String username) {
        return employeeRepository.findByEmail(username)
                .map(this::getResponse);
    }

    @SneakyThrows
    private List<SpecialRequestType> getResponse(Employee employee){
        List<SpecialRequestType> response = new ArrayList<>();
        response.add(SpecialRequestType.SICK);
        response.add(SpecialRequestType.SICK_WITH_MEDICAL_LETTER);
        response.add(SpecialRequestType.MARRIAGE);
        response.add(SpecialRequestType.MATERNITY);
        response.add(SpecialRequestType.CHILDBIRTH);
        response.add(SpecialRequestType.MAIN_FAMILY_DEATH);
        response.add(SpecialRequestType.CLOSE_FAMILY_DEATH);
        response.add(SpecialRequestType.HAJJ);
        response.add(SpecialRequestType.CHILD_BAPTISM);
        response.add(SpecialRequestType.CHILD_CIRCUMSION);
        response.add(SpecialRequestType.UNPAID_LEAVE);

        return response;
    }

}
