package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;

import java.util.List;

public interface GetIncomingRequestCommand extends Command<GetIncomingRequest, List<IncomingRequestResponse>> {

}
