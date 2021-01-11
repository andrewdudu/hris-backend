package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.AddSubstituteLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.SubstituteLeaveRequest;
import com.bliblifuture.hrisbackend.model.response.SubstituteLeaveResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AddSubstituteLeaveCommandImpl implements AddSubstituteLeaveCommand {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<SubstituteLeaveResponse> execute(SubstituteLeaveRequest request) {
        Date currentDate = dateUtil.getNewDate();

        return userRepository.findFirstByUsername(request.getRequester())
                .flatMap(user -> createLeave(request, user, currentDate)
                        .flatMap(leave -> leaveRepository.save(leave))
                        .collectList()
                )
                .flatMap(leaves -> createResponse(request.getId(), currentDate));
    }

    private Mono<SubstituteLeaveResponse> createResponse(String employeeId, Date currentDate) {
        SubstituteLeaveResponse response = SubstituteLeaveResponse.builder()
                .id(employeeId)
                .build();
        return leaveRepository
                .countByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThan(
                        employeeId, LeaveType.substitute, currentDate, 0)
                .map(remaining -> {
                    response.setTotal(Math.toIntExact(remaining));
                    return response;
                });
    }

    private Flux<Leave> createLeave(SubstituteLeaveRequest request, User user, Date currentDate) {
        Date expDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(90));

        List<Leave> leaves = new ArrayList<>();
        for (int i = 0; i < request.getTotal(); i++) {
            Leave leave = Leave.builder()
                    .code("SUBS")
                    .employeeId(request.getId())
                    .type(LeaveType.substitute)
                    .expDate(expDate)
                    .remaining(1)
                    .used(0)
                    .build();
            leave.setId(uuidUtil.getNewID());
            leave.setCreatedBy(user.getUsername());
            leave.setCreatedDate(currentDate);

            leaves.add(leave);
        }
        return Flux.fromIterable(leaves);
    }

}
