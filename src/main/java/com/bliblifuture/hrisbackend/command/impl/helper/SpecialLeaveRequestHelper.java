package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpecialLeaveRequestHelper {

    public Mono<Request> processRequest(LeaveRequestData data, User user, long currentDateTime){
        return Mono.fromCallable(() -> createRequest(data, user, currentDateTime));
    }

    @SneakyThrows
    private Request createRequest(LeaveRequestData data, User user, long currentDateTime) {
        if (data.getType().equals(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString())){
            if (data.getFiles() == null || data.getFiles().isEmpty()){
                String errorsMessage = "files=INVALID_REQUEST";
                throw new RuntimeException(errorsMessage);
            }
        }

        List<Date> dates = new ArrayList<>();
        for (String dateString : data.getDates()) {
            Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
            dates.add(date);
        }

        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .dates(dates)
                .notes(data.getNotes())
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.valueOf(data.getType()))
                .status(RequestStatus.REQUESTED)
                .build();

        request.setId(request.getSpecialLeaveType().toString() + "-" + request.getEmployeeId() + "-" + currentDateTime);
        request.setCreatedDate(new Date(currentDateTime));
        request.setCreatedBy(user.getUsername());

        if (data.getFiles() != null){
            List<String> files = FileHelper.saveFiles(data, user.getEmployeeId(), currentDateTime);
            request.setFiles(files);
        }

        return request;
    }
}
