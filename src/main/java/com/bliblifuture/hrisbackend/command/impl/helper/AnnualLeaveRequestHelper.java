package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnnualLeaveRequestHelper {

    public Request processRequest(LeaveRequestData data, User user, List<Leave> leaves, long currentDateTime){
        checkRemainingLeave(leaves, data);
        return createRequest(data, user, currentDateTime);
    }

    private void checkRemainingLeave(List<Leave> leaves, LeaveRequestData data) {
        int remaining = 0;
        for (Leave leave: leaves) {
            remaining += leave.getRemaining();
        }

        if (remaining < data.getDates().size()){
            String errorsMessage = "type=QUOTA_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Request createRequest(LeaveRequestData data, User user, long currentDateTime) {
        List<Date> dates = new ArrayList<>();
        for (String dateString : data.getDates()) {
            try {
                Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
                dates.add(date);
            }
            catch (Exception e){
                String errorsMessage = "date=INVALID_FORMAT";
                throw new IllegalArgumentException(errorsMessage);
            }
        }

        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .dates(dates)
                .notes(data.getNotes())
                .type(RequestType.valueOf(data.getType()))
                .status(RequestStatus.REQUESTED)
                .build();
        request.setId("ANNUAL-" + user.getEmployeeId() + currentDateTime);
        request.setCreatedBy(user.getUsername());
        request.setCreatedDate(new Date(currentDateTime));

        if (data.getFiles() != null){
            List<String> files = FileHelper.saveFiles(data, user.getEmployeeId(), currentDateTime);
            request.setFiles(files);
        }

        return request;
    }

}
