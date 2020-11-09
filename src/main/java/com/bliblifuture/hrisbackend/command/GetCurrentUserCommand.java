package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.response.UserResponse;

public interface GetCurrentUserCommand extends Command<String, UserResponse> {

}
