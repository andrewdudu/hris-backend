package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;

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
            String errorsMessage = "type=QUOTA_NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    @SneakyThrows
    private Request createRequest(LeaveRequestData data, User user, long currentDateTime) {
        List<Date> dates = new ArrayList<>();
        for (String dateString : data.getDates()) {
            Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
            dates.add(date);
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

        return request;
    }

}
