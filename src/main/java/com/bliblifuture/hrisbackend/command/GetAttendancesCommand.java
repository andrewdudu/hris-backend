package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.AttendanceListRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;

import java.util.List;

public interface GetAttendancesCommand extends Command<AttendanceListRequest, List<AttendanceResponse>> {

}
