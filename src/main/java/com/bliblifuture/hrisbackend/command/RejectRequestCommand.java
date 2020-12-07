package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;

public interface RejectRequestCommand extends Command<BaseRequest, IncomingRequestResponse> {

}
