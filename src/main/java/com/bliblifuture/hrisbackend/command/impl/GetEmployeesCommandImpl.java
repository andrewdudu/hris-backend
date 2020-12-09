package com.bliblifuture.hrisbackend.command.impl;

import com.blibli.oss.common.paging.Paging;
import com.bliblifuture.hrisbackend.command.GetEmployeesCommand;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetEmployeesCommandImpl implements GetEmployeesCommand {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @SneakyThrows
    @Override
    public Mono<PagingResponse<EmployeeResponse>> execute(EmployeesRequest request) {
//        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        PagingResponse<EmployeeResponse> response = new PagingResponse<>();

        return departmentRepository.findByName(request.getDepartment())
                .doOnSuccess(this::checkNull)
                .flatMap(department -> employeeRepository.findByDepId(department.getId())
                        .flatMap(employee -> createResponse(employee, department))
                        .collectList()
                )
                .map(employeeResponseList -> {
                    response.setData(employeeResponseList);
                    response.setPaging(Paging.builder().build());
                    return response;
                });
    }

    private Mono<EmployeeResponse> createResponse(Employee employee, Department department){
        EmployeeResponse response = EmployeeResponse.builder()
                .name(employee.getName())
                .department(department.getName())
                .id(employee.getId())
                .gender(employee.getGender())
                .build();

        return officeRepository.findById(employee.getOfficeId())
                .map(office -> {
                    response.setOffice(OfficeResponse.builder().name(office.getName()).build());
                    return response;
                });
    }

    private void checkNull(Department department) {
        if (department == null){
            String msg = "department=INVALID_REQUEST";
            throw new IllegalArgumentException(msg);
        }
    }

}
