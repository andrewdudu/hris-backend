package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.EmployeeDetailResponse;

public interface GetEmployeeDetailCommand extends Command<String, EmployeeDetailResponse> {

}
