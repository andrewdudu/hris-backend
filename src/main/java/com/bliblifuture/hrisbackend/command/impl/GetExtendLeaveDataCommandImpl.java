package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.ExtendLeaveQuotaResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class GetExtendLeaveDataCommandImpl implements GetExtendLeaveDataCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    @SneakyThrows
    public Mono<ExtendLeaveResponse> execute(String username) {
        Date currentDate = dateUtil.getNewDate();
        int year = currentDate.getYear() + 1900;
        Date extensionDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse((year+1) + "-3-1 00:00:00");

        return userRepository.findFirstByUsername(username)
                .flatMap(user -> requestRepository.findFirstByEmployeeIdAndTypeAndDatesContains(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, extensionDate)
                        .switchIfEmpty(Mono.just(Request.builder().build()))
                        .map(leaveRequest -> setResponseStatus(leaveRequest, currentDate))
                        .flatMap(response -> setResponseQuota(response, user.getEmployeeId(), currentDate, extensionDate))
                );
    }

    private ExtendLeaveResponse setResponseStatus(Request request, Date currentDate) {
        ExtendLeaveResponse response = ExtendLeaveResponse.builder().build();
        if (request.getId() != null){
            response.setStatus(request.getStatus());
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
                .switchIfEmpty(Flux.empty())
                .collectList()
                .map(leaves -> checkNonAvailableQuota(leaves, response, extensionDate));
    }

    private ExtendLeaveResponse checkNonAvailableQuota(List<Leave> leaves, ExtendLeaveResponse response, Date extensionDate) {
        if (leaves.size() == 0 || leaves.get(0).getRemaining() == 0){
            response.setQuota(ExtendLeaveQuotaResponse.builder()
                    .extensionDate(extensionDate)
                    .remaining(0)
                    .build());
            response.setStatus(RequestStatus.UNAVAILABLE);
        }
        else {
            response.setQuota(ExtendLeaveQuotaResponse.builder()
                    .extensionDate(extensionDate)
                    .remaining(leaves.get(0).getRemaining())
                    .build());
        }
        return response;
    }
}
