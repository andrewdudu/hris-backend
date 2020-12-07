package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestExtendLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
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
    private RequestRepository requestRepository;

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
                .flatMap(user -> requestRepository.findByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED)
                        .doOnNext(this::checkRequest)
                        .switchIfEmpty(Mono.just(createLeaveRequest(request, currentDate, user)))
                        .flatMap(leaveRequest -> requestRepository.save(leaveRequest))
                        .map(leaveRequest -> {
                            response.setStatus(leaveRequest.getStatus());
                            return response;
                }));
    }

    private void checkRequest(Request req) {
        if (req != null){
            String msg = "status=REQUESTED";
            throw new IllegalArgumentException(msg);
        }
    }

    private Request createLeaveRequest(LeaveRequestData request, Date currentDate, User user) {
        String employeeId = user.getEmployeeId();

        Request leaveRequest = Request.builder()
                .status(RequestStatus.REQUESTED)
                .type(RequestType.EXTEND_ANNUAL_LEAVE)
                .notes(request.getNotes())
                .employeeId(employeeId)
                .build();

        leaveRequest.setId("REQ_EXT-" + employeeId + "-" +currentDate.getTime());
        leaveRequest.setCreatedBy(user.getUsername());
        leaveRequest.setCreatedDate(currentDate);

        return leaveRequest;
    }

}
