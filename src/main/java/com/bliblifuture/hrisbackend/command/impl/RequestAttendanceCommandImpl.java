package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequestData;
import com.bliblifuture.hrisbackend.model.response.AttendanceRequestResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
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

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<AttendanceRequestResponse> execute(AttendanceRequestData request) {
        return userRepository.findByUsername(request.getRequester())
                .map(user -> createRequestEntity(request, user))
                .flatMap(entity -> requestRepository.save(entity))
                .map(this::createResponse);
    }

    private AttendanceRequestResponse createResponse(Request attendanceRequest) {
        String date = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(attendanceRequest.getDates().get(0));
        String clockIn = new SimpleDateFormat(DateUtil.TIME_FORMAT).format(attendanceRequest.getClockIn());
        String clockOut = new SimpleDateFormat(DateUtil.TIME_FORMAT).format(attendanceRequest.getClockOut());

        AttendanceRequestResponse response = AttendanceRequestResponse.builder()
                .date(date)
                .ClockIn(clockIn)
                .ClockOut(clockOut)
                .notes(attendanceRequest.getNotes())
                .build();
        response.setId(attendanceRequest.getId());

        return response;
    }

    private Request createRequestEntity(AttendanceRequestData data, User user){
        Date clockIn, clockOut, date;
        try {
            clockIn = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse(data.getDate() + " " + data.getClockIn() + ":00");
            clockOut = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                    .parse(data.getDate() + " " + data.getClockOut() + ":00");
            date = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                    .parse(data.getDate());
        }
        catch (Exception e){
            String msg = "message=INTERNAL_ERROR";
            throw new RuntimeException(msg);
        }
        Request request = Request.builder()
                .clockIn(clockIn)
                .clockOut(clockOut)
                .dates(Collections.singletonList(date))
                .notes(data.getNotes())
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .build();
        Date currentDateTime = dateUtil.getNewDate();
        request.setCreatedDate(currentDateTime);
        request.setCreatedBy(user.getUsername());

        request.setId("REQ_ATT-" + user.getEmployeeId() + "-" + currentDateTime.getTime());

        return request;
    }

}
