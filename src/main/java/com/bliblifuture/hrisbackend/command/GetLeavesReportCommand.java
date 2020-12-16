package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.LeaveReportResponse;

public interface GetLeavesReportCommand extends Command<String, LeaveReportResponse> {

}
