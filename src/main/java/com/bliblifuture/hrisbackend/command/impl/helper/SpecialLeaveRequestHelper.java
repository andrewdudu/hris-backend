package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpecialLeaveRequestHelper {

    public Mono<Request> processRequest(LeaveRequestData data, User user, long currentDateTime){
        return Mono.fromCallable(() -> createRequest(data, user.getEmployeeId(), currentDateTime));
    }

    private Request createRequest(LeaveRequestData data, String employeeId, long currentDateTime) {
        if (data.getType().equals(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString())){
            if (data.getFiles() == null || data.getFiles().isEmpty()){
                String errorsMessage = "files=INVALID_REQUEST";
                throw new RuntimeException(errorsMessage);
            }
        }

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
                .employeeId(employeeId)
                .dates(dates)
                .notes(data.getNotes())
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.valueOf(data.getType()))
                .status(RequestStatus.REQUESTED)
                .build();

        request.setId(request.getSpecialLeaveType().toString() + "-" + request.getEmployeeId() + "-" + currentDateTime);

        if (data.getFiles() != null){
            List<String> files = FileHelper.saveFiles(data, employeeId, currentDateTime);
            request.setFiles(files);
        }

        return request;
    }
}
