package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.*;
import com.bliblifuture.hrisbackend.constant.LeaveTypeConstant;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class RequestLeaveCommandImpl implements RequestLeaveCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Override
    public Mono<LeaveRequestResponse> execute(LeaveRequestData request) {
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> callHelper(request, user))
                .flatMap(leaveRequest -> leaveRequestRepository.save(leaveRequest))
                .map(RequestLeaveCommandImpl::createResponse);
    }

    private Mono<LeaveRequest> callHelper(LeaveRequestData request, User user) {
        RequestHelper helper;
        switch (request.getType()){
            case LeaveTypeConstant.ANNUAL_LEAVE:
                helper = new AnnualLeaveRequestHelper();
                break;
            case LeaveTypeConstant.SUBTITUTE_LEAVE:
                helper = new SubtituteLeaveRequestHelper();
                break;
            case LeaveTypeConstant.EXTRA_LEAVE:
                helper = new ExtraLeaveRequestHelper();
                break;
            case LeaveTypeConstant.CHILD_BAPTISM:
            case LeaveTypeConstant.CHILD_CIRCUMSION:
            case LeaveTypeConstant.CHILDBIRTH:
            case LeaveTypeConstant.CLOSE_FAMILY_DEATH:
            case LeaveTypeConstant.MAIN_FAMILY_DEATH:
            case LeaveTypeConstant.HAJJ:
            case LeaveTypeConstant.MARRIAGE:
            case LeaveTypeConstant.MATERNITY:
            case LeaveTypeConstant.SICK:
            case LeaveTypeConstant.SICK_WITH_MEDICAL_LETTER:
            case LeaveTypeConstant.UNPAID_LEAVE:
                helper = new SpecialLeaveRequestHelper();
                break;
            default:
                throw new IllegalArgumentException("INVALID_REQUEST");
        }
        return helper.processRequest(request, user);
    }

    private static LeaveRequestResponse createResponse(LeaveRequest leaveRequest) {
        List<String> dates = new ArrayList<>();
        for (Date dateString : leaveRequest.getDates()) {
            String date = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(dateString);
            dates.add(date);
        }

        return LeaveRequestResponse
                .builder()
                .files(Collections.singletonList(""))
                .dates(dates)
                .notes(leaveRequest.getNotes())
                .build();
    }

}
