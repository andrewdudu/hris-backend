package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.model.response.util.AttendanceTimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class RequestResponseHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResponseHelper userResponseHelper;

    public Mono<RequestResponse> createResponse(Request request) {
        RequestResponse response = RequestResponse.builder()
                .date(request.getCreatedDate())
                .status(request.getStatus())
                .approvedby(request.getApprovedBy())
                .build();
        response.setId(request.getId());

        return userRepository.findByEmployeeId(request.getEmployeeId())
                .flatMap(user -> userResponseHelper.getUserResponse(user))
                .map(userResponse -> {
                    response.setUser(userResponse);
                    return setDetailResponse(response, request);
                });
    }

    private RequestResponse setDetailResponse(RequestResponse response, Request request) {
        switch (request.getType()) {
            case ATTENDANCE:
                response.setType(RequestType.ATTENDANCE);
                return setAttendanceRequestResponse(response, request);
            case EXTEND_ANNUAL_LEAVE:
                response.setType(RequestType.EXTEND);
                return setExtendLeaveRequestResponse(response, request);
            case ANNUAL_LEAVE:
            case EXTRA_LEAVE:
            case SPECIAL_LEAVE:
            case SUBSTITUTE_LEAVE:
                response.setType(RequestType.LEAVE);
                return setLeaveRequestResponse(response, request);
            default:
                throw new IllegalArgumentException("INTERNAL_DATA_ERROR");
        }
    }

    private RequestResponse setAttendanceRequestResponse(RequestResponse response, Request request) {
        AttendanceTimeResponse date = AttendanceTimeResponse.builder()
                .start(request.getClockIn())
                .end(request.getClockOut())
                .build();

        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();

        response.setDetail(detail);

        return response;
    }

    private RequestResponse setExtendLeaveRequestResponse(RequestResponse response, Request request) {
        ExtendLeaveResponse extend = ExtendLeaveResponse.builder()
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .extend(extend)
                .build();

        response.setDetail(detail);

        return response;
    }

    private RequestResponse setLeaveRequestResponse(RequestResponse response, Request request) {
        LeaveRequestResponse leave = LeaveRequestResponse.builder()
                .dates(convertDatesToString(request.getDates()))
                .files(request.getFiles())
                .notes(request.getNotes())
                .build();
        if (request.getType().equals(RequestType.SPECIAL_LEAVE)){
            leave.setType(request.getSpecialLeaveType().toString());
        }
        else {
            leave.setType(request.getType().toString());
        }

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        response.setDetail(detail);

        return response;
    }

    private List<String> convertDatesToString(List<Date> dates) {
        List<String> datesString = new ArrayList<>();
        for (Date date : dates) {
            datesString.add(new SimpleDateFormat(DateUtil.DATE_FORMAT).format(date));
        }
        return datesString;
    }

}
