package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.GetEmployeeDetailCommand;
import com.bliblifuture.hrisbackend.command.GetEmployeesCommand;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.EmployeeDetailResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.mongodb.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employees")
    public Mono<Response<List<EmployeeResponse>>> getEmployees(@RequestParam(value = "department", required = false) @Nullable String department,
                                                               @RequestParam(value = "name", required = false) String name,
                                                               @RequestParam("page") int page, @RequestParam("size") int size){
        EmployeesRequest request = new EmployeesRequest();
        if (department != null){
            request.setDepartment(department.toUpperCase());
        }
        request.setName(name);
        request.setPage(page);
        request.setSize(size);
        return commandExecutor.execute(GetEmployeesCommand.class, request)
                .map(pagingResponse -> {
                    Response<List<EmployeeResponse>> response = ResponseHelper.ok(pagingResponse.getData());
                    response.setPaging(pagingResponse.getPaging());
                    return response;
                })
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employee/{id}")
    public Mono<Response<EmployeeDetailResponse>> getEmployeeDetail(@PathVariable("id") String id){
        return commandExecutor.execute(GetEmployeeDetailCommand.class, id)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

}
