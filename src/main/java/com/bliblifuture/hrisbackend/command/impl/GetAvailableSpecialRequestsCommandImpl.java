package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableSpecialRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAvailableSpecialRequestsCommandImpl implements GetAvailableSpecialRequestsCommand {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Mono<List<SpecialLeaveType>> execute(String username) {
        return employeeRepository.findFirstByEmail(username)
                .map(this::getResponse);
    }

    @SneakyThrows
    private List<SpecialLeaveType> getResponse(Employee employee){
        List<SpecialLeaveType> response = new ArrayList<>();
        response.add(SpecialLeaveType.SICK);
        response.add(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER);
        response.add(SpecialLeaveType.MARRIAGE);
        response.add(SpecialLeaveType.MATERNITY);
        response.add(SpecialLeaveType.CHILDBIRTH);
        response.add(SpecialLeaveType.MAIN_FAMILY_DEATH);
        response.add(SpecialLeaveType.CLOSE_FAMILY_DEATH);
        response.add(SpecialLeaveType.HAJJ);
        response.add(SpecialLeaveType.CHILD_BAPTISM);
        response.add(SpecialLeaveType.CHILD_CIRCUMSION);
        response.add(SpecialLeaveType.UNPAID_LEAVE);

        return response;
    }

}
