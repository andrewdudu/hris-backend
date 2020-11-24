package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpecialLeaveRequestHelper {

    public Mono<LeaveRequest> processRequest(LeaveRequestData data, User user, long currentDateTime){
        return Mono.fromCallable(() -> createRequest(data, user.getEmployeeId(), currentDateTime));
    }

    private LeaveRequest createRequest(LeaveRequestData data, String employeeId, long currentDateTime) {
        if (data.getType().equals(SpecialLeaveType.SICK.toString())){
            if (data.getDates().size() > 1){
                throw new IllegalArgumentException("INVALID");
            }
        }

        List<Date> dates = new ArrayList<>();
        for (String dateString : data.getDates()) {
            try {
                Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
                dates.add(date);
            }
            catch (Exception e){
                throw new IllegalArgumentException("INVALID_REQUEST");
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(employeeId)
                .dates(dates)
                .notes(data.getNotes())
                .type(RequestLeaveType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.valueOf(data.getType()))
                .status(RequestStatus.REQUESTED)
                .build();

        leaveRequest.setId(leaveRequest.getSpecialLeaveType().toString() + "-" + leaveRequest.getEmployeeId() + "-" + currentDateTime);

        if (data.getFiles() != null){
            List<String> files = FileHelper.saveFiles(data, employeeId, currentDateTime);
            leaveRequest.setFiles(files);
        }

        return leaveRequest;
    }
}
