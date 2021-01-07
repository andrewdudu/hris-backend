package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.SubstituteLeaveRequest;
import com.bliblifuture.hrisbackend.model.response.SubstituteLeaveResponse;

public interface AddSubstituteLeaveCommand extends Command<SubstituteLeaveRequest, SubstituteLeaveResponse> {

}
