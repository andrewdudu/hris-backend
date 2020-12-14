package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.GetCalendarRequest;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;

import java.util.List;

public interface GetCalendarCommand extends Command<GetCalendarRequest, List<CalendarResponse>> {

}
