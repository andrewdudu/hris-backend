package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesQuotaCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GetLeavesQuotaCommandImpl implements GetLeavesQuotaCommand {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<List<LeaveResponse>> execute(String employeeId) {
        Date currentDate = dateUtil.getNewDate();
        Date startOfYear = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse((currentDate.getYear()+1900) + "-01-01");

        return leaveRepository.findByEmployeeIdAndExpDateAfter(employeeId, currentDate)
                .collectList()
                .map(this::createResponse)
                .flatMap(response -> requestRepository.countByEmployeeIdAndTypeAndStatusAndCreatedDateAfter(
                        employeeId, RequestType.ANNUAL_LEAVE, RequestStatus.APPROVED, startOfYear)
                        .map(annualLeaveUsed -> {
                            response.get(0).setUsed(Math.toIntExact(annualLeaveUsed));
                            return response;
                        })
                );
    }

    private List<LeaveResponse> createResponse(List<Leave> leaves) {
        List<LeaveResponse> responses = Arrays.asList(
                LeaveResponse.builder().type(LeaveType.annual)
                        .expiries(new ArrayList<>()).remaining(0).used(0).build(),
                LeaveResponse.builder().type(LeaveType.extra).remaining(0).used(0).build(),
                LeaveResponse.builder().type(LeaveType.substitute)
                        .expiries(new ArrayList<>()).remaining(0).used(0).build()
        );

        for (Leave leave : leaves) {
            if (leave.getType().equals(LeaveType.annual)){
                responses.get(0).setRemaining(responses.get(0).getRemaining() + leave.getRemaining());
                responses.get(0).setUsed(responses.get(0).getUsed() + leave.getUsed());
                if (leave.getRemaining() > 0){
                    responses.get(0).getExpiries().add(leave.getExpDate());
                }
            }
            else if (leave.getType().equals(LeaveType.extra)){
                responses.get(1).setRemaining(leave.getRemaining());
                responses.get(1).setUsed(leave.getUsed());
                responses.get(1).setExpiry(leave.getExpDate());
            }
            else {
                LeaveResponse substituteResponse = responses.get(2);
                int remaining = leave.getRemaining();
                int used = leave.getUsed();
                if (remaining > 0){
                    substituteResponse.getExpiries().add(leave.getExpDate());
                }
                substituteResponse.setRemaining(substituteResponse.getRemaining() + remaining);
                substituteResponse.setUsed(substituteResponse.getUsed() + used);
            }
        }

        return responses;
    }

}
