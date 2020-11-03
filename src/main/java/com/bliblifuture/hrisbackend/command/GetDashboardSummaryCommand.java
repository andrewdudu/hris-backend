package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;

public interface GetDashboardSummaryCommand extends Command<String, DashboardResponse> {

}
