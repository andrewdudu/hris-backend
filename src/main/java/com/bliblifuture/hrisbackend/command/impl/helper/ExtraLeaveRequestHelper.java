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

public class ExtraLeaveRequestHelper {

    public Request processRequest(LeaveRequestData data, User user, Leave leave, long currentDateTime){
        checkRemainingLeave(leave, data);
        return createRequest(data, user, currentDateTime);
    }

    private void checkRemainingLeave(Leave leave, LeaveRequestData data) {
        if (leave.getRemaining() < data.getDates().size()){
            String errorsMessage = "dates=QUOTA_NOT_AVAILABLE";
            throw new RuntimeException(errorsMessage);
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
                throw new RuntimeException(errorsMessage);
            }
        }

        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .dates(dates)
                .notes(data.getNotes())
                .type(RequestType.valueOf(data.getType()))
                .status(RequestStatus.REQUESTED)
                .build();
        request.setId("EXTRA-" + user.getEmployeeId() + currentDateTime);
        request.setCreatedBy(user.getUsername());
        request.setCreatedDate(new Date(currentDateTime));

        if (data.getFiles() != null){
            List<String> files = FileHelper.saveFiles(data, user.getEmployeeId(), currentDateTime);
            request.setFiles(files);
        }

        return request;
    }

}
