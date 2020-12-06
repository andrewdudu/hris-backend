package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.*;
import com.bliblifuture.hrisbackend.constant.LeaveTypeConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveResponse;
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
    public Mono<RequestLeaveResponse> execute(LeaveRequestData request) {
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
                        .map(leaves -> new SubstituteLeaveRequestHelper().processRequest(request, user, leaves, currentDateTime));
            case LeaveTypeConstant.EXTRA_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfter(user.getEmployeeId(), LeaveType.extra, dateUtil.getNewDate())
                        .map(leave -> new ExtraLeaveRequestHelper().processRequest(request, user, leave, currentDateTime));
            case LeaveTypeConstant.CLOSE_FAMILY_DEATH:
            case LeaveTypeConstant.SICK:
                if (request.getDates().size() > 1){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.CHILD_BAPTISM:
            case LeaveTypeConstant.CHILDBIRTH:
            case LeaveTypeConstant.MAIN_FAMILY_DEATH:
            case LeaveTypeConstant.SICK_WITH_MEDICAL_LETTER:
                if (request.getDates().size() > 2){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.CHILD_CIRCUMSION:
            case LeaveTypeConstant.MARRIAGE:
                if (request.getDates().size() > 3){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.HAJJ:
                if (request.getDates().size() > 30){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.MATERNITY:
                if (request.getDates().size() > 90){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.UNPAID_LEAVE:
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            default:
                String msg = "type=INVALID_FORMAT";
                throw new IllegalArgumentException(msg);
        }
    }

    private static RequestLeaveResponse createResponse(Request request) {
        List<String> dates = new ArrayList<>();
        for (Date dateString : request.getDates()) {
            String date = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(dateString);
            dates.add(date);
        }

        RequestLeaveResponse response = RequestLeaveResponse.builder()
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
