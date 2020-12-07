package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.SetHolidayRequest;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;

public interface SetHolidayCommand extends Command<SetHolidayRequest, EventDetailResponse> {

}
