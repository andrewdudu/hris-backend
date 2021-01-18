package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeaveQuotaFormCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.request.LeaveQuotaFormRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveQuotaFormResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
public class GetLeaveQuotaFormCommandImpl implements GetLeaveQuotaFormCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<LeaveQuotaFormResponse> execute(LeaveQuotaFormRequest request) {
        LeaveType type = getLeaveType(request.getCode());
        Date currentDate = dateUtil.getNewDate();
        return userRepository.findFirstByUsername(request.getRequester())
                .flatMap(user -> leaveRepository
                        .findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                           user.getEmployeeId(), type, currentDate
                        )
                        .collectList()
                        .map(this::createResponse)
                );
    }

    private LeaveQuotaFormResponse createResponse(List<Leave> leaves) {
        LeaveQuotaFormResponse response = LeaveQuotaFormResponse.builder().build();
        for (Leave l:leaves) {
            response.setLeaveQuota(l.getRemaining() + response.getLeaveQuota());
        }
        return response;
    }

    private LeaveType getLeaveType(String code) {
        switch (code){
            case "ANNUAL_LEAVE":
                return LeaveType.annual;
            case "EXTRA_LEAVE":
                return LeaveType.extra;
            case "SUBSTITUTE_LEAVE":
                return LeaveType.substitute;
            default:
                String msg = "code=INVALID_REQUEST";
                throw new IllegalArgumentException(msg);
        }
    }

}
