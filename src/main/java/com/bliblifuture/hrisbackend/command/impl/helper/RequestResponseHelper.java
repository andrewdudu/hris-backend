package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
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

    public Mono<IncomingRequestResponse> createResponse(Request request) {
        IncomingRequestResponse response = IncomingRequestResponse.builder()
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

    private IncomingRequestResponse setDetailResponse(IncomingRequestResponse response, Request request) {
        if (request.getType() != null){
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
                case HOURLY_LEAVE:
                    response.setType(RequestType.LEAVE);
                    return setHourlyLeaveResponse(response, request);
                default:
                    String errorsMessage = "type=INVALID_REQUEST";
                    throw new RuntimeException(errorsMessage);
            }
        }
        String errorsMessage = "type=INVALID_REQUEST";
        throw new RuntimeException(errorsMessage);
    }

    private IncomingRequestResponse setHourlyLeaveResponse(IncomingRequestResponse response, Request request) {
        RequestLeaveResponse leave = RequestLeaveResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType().toString())
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        response.setDetail(detail);

        return response;
    }

    private IncomingRequestResponse setAttendanceRequestResponse(IncomingRequestResponse response, Request request) {
        TimeResponse date = TimeResponse.builder()
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

    private IncomingRequestResponse setExtendLeaveRequestResponse(IncomingRequestResponse response, Request request) {
        ExtendLeaveResponse extend = ExtendLeaveResponse.builder()
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .extend(extend)
                .build();

        response.setDetail(detail);

        return response;
    }

    private IncomingRequestResponse setLeaveRequestResponse(IncomingRequestResponse response, Request request) {
        RequestLeaveResponse leave = RequestLeaveResponse.builder()
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
