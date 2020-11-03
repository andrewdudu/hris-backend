package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.entity.UserEntity;

public interface GetUserDetailsByUsernameCommand extends Command<String, UserEntity> {

}
