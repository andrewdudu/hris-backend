package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;

import java.util.List;

public interface GetAvailableRequestsCommand extends Command<String, List<RequestType>> {

}
