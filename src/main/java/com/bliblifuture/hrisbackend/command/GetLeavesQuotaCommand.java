package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;

import java.util.List;

public interface GetLeavesQuotaCommand extends Command<String, List<LeaveResponse>> {

}
