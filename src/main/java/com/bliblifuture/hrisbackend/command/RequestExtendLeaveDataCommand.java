package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;

public interface RequestExtendLeaveDataCommand extends Command<LeaveRequestData, ExtendLeaveResponse> {

}
