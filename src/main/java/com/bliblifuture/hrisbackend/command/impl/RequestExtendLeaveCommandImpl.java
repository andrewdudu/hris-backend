package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestExtendLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
public class RequestExtendLeaveCommandImpl implements RequestExtendLeaveCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    @SneakyThrows
    public Mono<ExtendLeaveResponse> execute(LeaveRequestData request) {
        ExtendLeaveResponse response = ExtendLeaveResponse.builder()
                .notes(request.getNotes())
                .build();

        Date currentDate = dateUtil.getNewDate();
        if (currentDate.getMonth() != 11){
            response.setStatus(RequestStatus.UNAVAILABLE);
            return Mono.just(response);
        }

        return userRepository.findByUsername(request.getRequester())
                .map(user -> createLeaveRequest(request, currentDate, user))
                .flatMap(leaveRequest -> leaveRequestRepository.save(leaveRequest))
                .map(leaveRequest -> {
                    response.setStatus(leaveRequest.getStatus());
                    return response;
                });
    }

    private LeaveRequest createLeaveRequest(LeaveRequestData request, Date currentDate, User user) {
        String employeeId = user.getEmployeeId();

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .status(RequestStatus.REQUESTED)
                .type(RequestLeaveType.EXTEND_ANNUAL_LEAVE)
                .notes(request.getNotes())
                .employeeId(employeeId)
                .build();

        leaveRequest.setId("REQ_EXT-" + employeeId + "-" +currentDate.getTime());
        leaveRequest.setCreatedBy(user.getUsername());
        leaveRequest.setCreatedDate(currentDate);

        return leaveRequest;
    }

}
