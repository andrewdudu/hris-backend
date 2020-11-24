package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;

public interface ClockInCommand extends Command<ClockInClockOutRequest, AttendanceResponse> {

}
