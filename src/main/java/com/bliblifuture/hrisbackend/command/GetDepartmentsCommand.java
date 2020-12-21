package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.util.DepartmentResponse;

import java.util.List;

public interface GetDepartmentsCommand extends Command<String, List<DepartmentResponse>> {

}
