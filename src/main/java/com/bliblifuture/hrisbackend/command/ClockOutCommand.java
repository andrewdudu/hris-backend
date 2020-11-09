package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;

public interface ClockOutCommand extends Command<AttendanceRequest, AttendanceResponse> {

}
