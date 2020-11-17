package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesQuotaCommand;
import com.bliblifuture.hrisbackend.constant.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @SneakyThrows
    @Override
    public Mono<List<LeaveResponse>> execute(String employeeId) {
        Date currentDate = new Date();
        int year = currentDate.getYear() + 1900;

        Date startOfThisYear = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .parse("1/1/" + year + " 00:00:00");

        return leaveRepository.findByEmployeeIdAndExpDateAfter(employeeId, startOfThisYear)
                .collectList()
                .map(this::createResponse);
    }

    private List<LeaveResponse> createResponse(List<Leave> leaves) {
        List<LeaveResponse> responses = Arrays.asList(
                LeaveResponse.builder().type(LeaveType.annual).build(),
                LeaveResponse.builder().type(LeaveType.extra).build(),
                LeaveResponse.builder().type(LeaveType.subtitute)
                        .expiries(new ArrayList<>()).remaining(0).used(0).build()
        );

        for (Leave leave : leaves) {
            if (leave.getType().equals(LeaveType.annual)){
                responses.get(0).setRemaining(leave.getRemaining());
                responses.get(0).setUsed(leave.getUsed());
                responses.get(0).setExpiry(leave.getExpDate());
            }
            else if (leave.getType().equals(LeaveType.extra)){
                responses.get(1).setRemaining(leave.getRemaining());
                responses.get(1).setUsed(leave.getUsed());
                responses.get(1).setExpiry(leave.getExpDate());
            }
            else {
                LeaveResponse subtituteResponse = responses.get(2);
                int remaining = leave.getRemaining();
                int used = leave.getUsed();
                if (remaining > 0){
                    subtituteResponse.setRemaining(remaining+1);
                    subtituteResponse.getExpiries().add(leave.getExpDate());
                }
                else{
                    subtituteResponse.setUsed(used);
                }
            }
        }

        return responses;
    }

}
