package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.BulkApproveRequest;
import com.bliblifuture.hrisbackend.model.response.BulkApproveResponse;

public interface BulkApproveRequestCommand extends Command<BulkApproveRequest, BulkApproveResponse> {

}
