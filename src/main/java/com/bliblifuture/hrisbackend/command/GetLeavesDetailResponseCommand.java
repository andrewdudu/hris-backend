package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.GetLeavesDetailRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveDetailResponse;

import java.util.List;

public interface GetLeavesDetailResponseCommand extends Command<GetLeavesDetailRequest, List<LeaveDetailResponse>> {

}
