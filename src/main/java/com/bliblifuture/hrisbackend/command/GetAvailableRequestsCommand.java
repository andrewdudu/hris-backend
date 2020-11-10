package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.constant.RequestType;

import java.util.List;

public interface GetAvailableRequestsCommand extends Command<String, List<RequestType>> {

}
