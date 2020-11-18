package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.AttendanceSummaryResponse;

import java.util.List;

public interface GetAttendanceSummaryCommand extends Command<String, List<AttendanceSummaryResponse>> {

}
