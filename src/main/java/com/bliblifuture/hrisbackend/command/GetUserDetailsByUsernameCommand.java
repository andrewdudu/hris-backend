package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.entity.User;

public interface GetUserDetailsByUsernameCommand extends Command<String, User> {

}
