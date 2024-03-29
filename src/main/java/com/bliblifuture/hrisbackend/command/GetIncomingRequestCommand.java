package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;

public interface GetIncomingRequestCommand extends Command<GetIncomingRequest, PagingResponse<RequestResponse>> {

}
