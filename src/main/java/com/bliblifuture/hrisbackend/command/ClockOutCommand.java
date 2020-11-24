package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.ClockInClockOutResponse;

public interface ClockOutCommand extends Command<ClockInClockOutRequest, ClockInClockOutResponse> {

}
