package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.constant.RequestStatus;
import com.bliblifuture.hrisbackend.constant.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.RequestAttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.RequestAttendanceResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Service
public class RequestAttendanceCommandImpl implements RequestAttendanceCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Override
    public Mono<RequestAttendanceResponse> execute(RequestAttendanceRequest request) {
        return userRepository.findByUsername(request.getRequester())
                .map(user -> createRequestEntity(request, user.getEmployeeId()))
                .flatMap(entity -> requestRepository.save(entity))
                .map(this::createResponse);
    }

    private RequestAttendanceResponse createResponse(Request entity) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(entity.getDates().get(0));
        RequestAttendanceResponse response = RequestAttendanceResponse
                .builder()
                .date(date)
                .ClockIn(entity.getClockIn())
                .ClockOut(entity.getClockOut())
                .notes(entity.getNotes())
                .build();
        return response;
    }

    private Request createRequestEntity(RequestAttendanceRequest data, String employeeId){
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(data.getDate());
        }
        catch (Exception e){
            throw new IllegalArgumentException("INVALID");
        }
        Request request = Request.builder()
                .type(RequestType.ATTENDANCE)
                .clockIn(data.getClockIn())
                .clockOut(data.getClockOut())
                .dates(Collections.singletonList(date))
                .notes(data.getNotes())
                .status(RequestStatus.REQUESTED)
                .employeeId(employeeId)
                .build();

        request.setId("REQ_ATT-" + employeeId + "-" + new Date().getTime());

        return request;
    }

}
