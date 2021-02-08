package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.LeaveQuotaFormRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveQuotaFormResponse;

public interface GetLeaveQuotaFormCommand extends Command<LeaveQuotaFormRequest, LeaveQuotaFormResponse> {

}
