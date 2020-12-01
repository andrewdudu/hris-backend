package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.UserReportResponse;

public interface GetAttendanceSummaryCommand extends Command<String, UserReportResponse> {

}
