package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.RequestAttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.RequestAttendanceResponse;

public interface RequestAttendanceCommand extends Command<RequestAttendanceRequest, RequestAttendanceResponse> {

}
