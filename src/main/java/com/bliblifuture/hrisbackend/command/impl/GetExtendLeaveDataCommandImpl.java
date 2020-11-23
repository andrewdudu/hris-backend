package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.ExtendLeaveQuotaResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class GetExtendLeaveDataCommandImpl implements GetExtendLeaveDataCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    @SneakyThrows
    public Mono<ExtendLeaveResponse> execute(String username) {
        Date currentDate = dateUtil.getNewDate();
        int year = currentDate.getYear() + 1900;
        Date extensionDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse((year+1) + "-3-1 00:00:00");

        return userRepository.findByUsername(username)
                .flatMap(user -> leaveRequestRepository
                        .findByEmployeeIdAndTypeAndDatesContains(user.getEmployeeId(), RequestLeaveType.EXTEND_ANNUAL_LEAVE, extensionDate)
                        .switchIfEmpty(Mono.just(LeaveRequest.builder().build()))
                        .map(leaveRequest -> setResponseStatus(leaveRequest, currentDate))
                        .flatMap(response -> setResponseQuota(response, user.getEmployeeId(), currentDate, extensionDate))
                );
    }

    private ExtendLeaveResponse setResponseStatus(LeaveRequest leaveRequest, Date currentDate) {
        ExtendLeaveResponse response = ExtendLeaveResponse.builder().build();
        if (leaveRequest.getId() != null){
            response.setStatus(leaveRequest.getStatus());
        }
        else {
            if (currentDate.getMonth() == 11){
                response.setStatus(RequestStatus.AVAILABLE);
            }
            else {
                response.setStatus(RequestStatus.UNAVAILABLE);
            }
        }
        return response;
    }

    @SneakyThrows
    private Mono<ExtendLeaveResponse> setResponseQuota(ExtendLeaveResponse response, String employeeId, Date currentDate, Date extensionDate){
        return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(employeeId, LeaveType.annual, currentDate)
                .collectList()
                .map(leave -> {
                    response.setQuota(ExtendLeaveQuotaResponse.builder()
                            .extensionDate(extensionDate)
                            .remaining(leave.get(0).getRemaining())
                            .build());
                    return response;
                });
    }
}
