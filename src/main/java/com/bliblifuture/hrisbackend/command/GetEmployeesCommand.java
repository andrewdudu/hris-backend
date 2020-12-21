package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;

public interface GetEmployeesCommand extends Command<EmployeesRequest, PagingResponse<EmployeeResponse>> {

}
