package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;

import java.util.List;

public interface GetIncomingRequestCommand extends Command<String, List<RequestResponse>> {

}
