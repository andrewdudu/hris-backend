package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;

public interface ApproveRequestCommand extends Command<BaseRequest, IncomingRequestResponse> {

}
