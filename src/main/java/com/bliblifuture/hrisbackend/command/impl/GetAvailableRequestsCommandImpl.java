package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.constant.RequestType;
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
    public Mono<List<RequestType>> execute(String username) {
        return employeeRepository.findByEmail(username)
                .map(this::getResponse);
    }

    @SneakyThrows
    private List<RequestType> getResponse(Employee employee){
        List<RequestType> response = new ArrayList<>();
        response.add(RequestType.ATTENDANCE);
        response.add(RequestType.ANNUAL_LEAVE);
        response.add(RequestType.SPECIAL_LEAVE);
        response.add(RequestType.SUBTITUTE_LEAVE);

        Date currentDate = dateUtil.getNewDate();

        int date = employee.getJoinDate().getDate();
        int month = employee.getJoinDate().getMonth()+1;
        int year = employee.getJoinDate().getYear()+1900;
        System.out.println(employee.getJoinDate());

        Date dateToGetExtraLeave = new SimpleDateFormat("dd/MM/yyyy").parse(date + "/" + month + "/" + (year+3));

        if (currentDate.after(dateToGetExtraLeave)){
            response.add(RequestType.EXTRA_LEAVE);
        }

        if (currentDate.getMonth() == Calendar.DECEMBER) {
            response.add(RequestType.EXTEND_ANNUAL_LEAVE);
        }

        return response;
    }

}
