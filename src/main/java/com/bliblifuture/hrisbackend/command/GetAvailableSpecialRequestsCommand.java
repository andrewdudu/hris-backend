package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;

import java.util.List;

public interface GetAvailableSpecialRequestsCommand extends Command<String, List<SpecialLeaveType>> {

}
