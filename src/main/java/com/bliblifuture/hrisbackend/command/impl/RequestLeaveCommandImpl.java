package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.*;
import com.bliblifuture.hrisbackend.constant.LeaveTypeConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RequestLeaveCommandImpl implements RequestLeaveCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<LeaveRequestResponse> execute(LeaveRequestData request) {
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> callHelper(request, user))
                .flatMap(leaveRequest -> requestRepository.save(leaveRequest))
                .map(RequestLeaveCommandImpl::createResponse);
    }

    private Mono<Request> callHelper(LeaveRequestData request, User user) {
        long currentDateTime = dateUtil.getNewDate().getTime();
        switch (request.getType()){
            case LeaveTypeConstant.ANNUAL_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), LeaveType.annual, dateUtil.getNewDate())
                        .collectList()
                        .map(leaves -> new AnnualLeaveRequestHelper().processRequest(request, user, leaves, currentDateTime));
            case LeaveTypeConstant.SUBSTITUTE_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThan(user.getEmployeeId(), LeaveType.substitute, dateUtil.getNewDate(), 0)
                        .collectList()
                        .map(leaves -> new SubtituteLeaveRequestHelper().processRequest(request, user, leaves, currentDateTime));
            case LeaveTypeConstant.EXTRA_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfter(user.getEmployeeId(), LeaveType.extra, dateUtil.getNewDate())
                        .map(leave -> new ExtraLeaveRequestHelper().processRequest(request, user, leave, currentDateTime));
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
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            default:
                throw new IllegalArgumentException("INVALID_TYPE_FORMAT");
        }
    }

    private static LeaveRequestResponse createResponse(Request request) {
        List<String> dates = new ArrayList<>();
        for (Date dateString : request.getDates()) {
            String date = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(dateString);
            dates.add(date);
        }

        LeaveRequestResponse response = LeaveRequestResponse.builder()
                .files(request.getFiles())
                .dates(dates)
                .notes(request.getNotes())
                .build();
        if (request.getType().equals(RequestType.SPECIAL_LEAVE)){
            response.setType(request.getSpecialLeaveType().toString());
        }
        else {
            response.setType(request.getType().toString());
        }
        response.setId(request.getId());

        return response;
    }

}
