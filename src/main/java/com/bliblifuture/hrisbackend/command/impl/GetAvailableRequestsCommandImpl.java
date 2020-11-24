package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class GetAvailableRequestsCommandImpl implements GetAvailableRequestsCommand {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<List<RequestLeaveType>> execute(String username) {
        return employeeRepository.findByEmail(username)
                .map(this::getResponse);
    }

    @SneakyThrows
    private List<RequestLeaveType> getResponse(Employee employee){
        List<RequestLeaveType> response = new ArrayList<>();
        response.add(RequestLeaveType.ATTENDANCE);
        response.add(RequestLeaveType.ANNUAL_LEAVE);
        response.add(RequestLeaveType.SPECIAL_LEAVE);
        response.add(RequestLeaveType.SUBTITUTE_LEAVE);

        Date currentDate = dateUtil.getNewDate();

        int date = employee.getJoinDate().getDate();
        int month = employee.getJoinDate().getMonth()+1;
        int year = employee.getJoinDate().getYear()+1900;
        System.out.println(employee.getJoinDate());

        Date dateToGetExtraLeave = new SimpleDateFormat("dd/MM/yyyy").parse(date + "/" + month + "/" + (year+3));

        if (currentDate.after(dateToGetExtraLeave)){
            response.add(RequestLeaveType.EXTRA_LEAVE);
        }

        if (currentDate.getMonth() == Calendar.DECEMBER) {
            response.add(RequestLeaveType.EXTEND_ANNUAL_LEAVE);
        }

        return response;
    }

}
