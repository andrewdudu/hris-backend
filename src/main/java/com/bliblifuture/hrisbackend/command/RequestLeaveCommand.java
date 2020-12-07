package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveResponse;

public interface RequestLeaveCommand extends Command<LeaveRequestData, RequestLeaveResponse> {

}
