package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDepartmentsCommand;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.response.util.DepartmentResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GetDepartmentsCommandImpl implements GetDepartmentsCommand {

    @Autowired
    private DepartmentRepository departmentRepository;

    @SneakyThrows
    @Override
    public Mono<List<DepartmentResponse>> execute(String request) {
        return departmentRepository.findAll()
                .map(this::createResponse)
                .collectList();
    }

    private DepartmentResponse createResponse(Department department) {
        return DepartmentResponse.builder()
                .name(department.getName())
                .code(department.getCode())
                .build();
    }

}
