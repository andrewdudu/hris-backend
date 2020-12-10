package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetEmployeesCommand;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeElasticsearchRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetEmployeesCommandImpl implements GetEmployeesCommand {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private EmployeeElasticsearchRepository employeeElasticsearchRepository;

    @SneakyThrows
    @Override
    public Mono<PagingResponse<EmployeeResponse>> execute(EmployeesRequest request) {
        return getEmployees(request)
                .map(employeeResponseList -> getPagingEmployee(employeeResponseList, request));
    }

    private PagingResponse<EmployeeResponse> getPagingEmployee(List<EmployeeResponse> employeeResponseList, EmployeesRequest request) {
        PagingResponse<EmployeeResponse> response = new PagingResponse<>();

        int page = request.getPage();
        int size = request.getSize();
        List<EmployeeResponse> pagingData = new ArrayList<>();

        int lowerbound = page * size;
        if (lowerbound > employeeResponseList.size()){
            String msg = "page=OUT_OF_BOUND";
            throw new IllegalArgumentException(msg);
        }

        int upperbound = lowerbound +size;
        if (upperbound > employeeResponseList.size()){
            upperbound = employeeResponseList.size();
        }

        for (int i = lowerbound; i < upperbound; i++) {
            pagingData.add(employeeResponseList.get(i));
        }
        response.setData(pagingData);

        return response.setPagingDetail(request, employeeResponseList.size());
    }

    private Mono<List<EmployeeResponse>> getEmployees(EmployeesRequest request){
        if (request.getDepartment() == null || request.getDepartment().isEmpty()) {
            if (request.getName() == null || request.getName().isEmpty()){
                return employeeRepository.findAll()
                        .flatMap(this::createResponse)
                        .collectList();
            }
            return employeeElasticsearchRepository.search("*" + request.getName().toLowerCase() + "*")
                    .map(employeeIndex -> {
                        System.out.println("masu");
                        return employeeIndex;
                    })
                    .flatMap(employeeIndex -> employeeRepository.findById(employeeIndex.getId()))
                    .flatMap(this::createResponse)
                    .collectList();
        }
        if (request.getName() == null || request.getName().isEmpty()){
            return departmentRepository.findByName(request.getDepartment())
                    .doOnSuccess(this::checkNull)
                    .flatMap(department -> employeeRepository.findByDepId(department.getId())
                            .flatMap(this::createResponse)
                            .collectList());
        }
        return departmentRepository.findByName(request.getDepartment())
                .doOnSuccess(this::checkNull)
                .flatMap(department -> employeeElasticsearchRepository.search("*" + request.getName().toLowerCase() + "*", department.getId().replace("-", ""))
                        .flatMap(employeeIndex -> employeeRepository.findById(employeeIndex.getId()))
                        .flatMap(this::createResponse)
                        .collectList()
                );
    }

    private Mono<EmployeeResponse> createResponse(Employee employee){
        EmployeeResponse response = EmployeeResponse.builder()
                .name(employee.getName())
                .id(employee.getId())
                .gender(employee.getGender())
                .build();

        return officeRepository.findById(employee.getOfficeId())
                .flatMap(office -> {
                    response.setOffice(OfficeResponse.builder().name(office.getName()).build());
                    return departmentRepository.findById(employee.getDepId())
                            .map(department -> {
                                response.setDepartment(department.getName());
                                return response;
                            });
                });
    }

    private void checkNull(Department department) {
        if (department == null){
            String msg = "department=INVALID_REQUEST";
            throw new IllegalArgumentException(msg);
        }
    }

}
