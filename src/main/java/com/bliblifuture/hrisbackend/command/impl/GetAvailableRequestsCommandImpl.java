package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<List<RequestType>> execute(String username) {
        return employeeRepository.findByEmail(username)
                .map(this::getResponse)
                .flatMap(response -> userRepository.findByUsername(username)
                        .map(user -> addAdminRequest(user, response))
                );
    }

    private List<RequestType> addAdminRequest(User user, List<RequestType> response) {
        if (user.getRoles().contains(UserRole.ADMIN)){
            response.add(RequestType.INCOMING_REQUESTS);
            response.add(RequestType.SET_HOLIDAY);
            response.add(RequestType.EMPLOYEE);
        }
        return response;
    }

    @SneakyThrows
    private List<RequestType> getResponse(Employee employee){
        List<RequestType> response = new ArrayList<>();
        response.add(RequestType.ATTENDANCE);
        response.add(RequestType.ANNUAL_LEAVE);
        response.add(RequestType.SPECIAL_LEAVE);
        response.add(RequestType.SUBSTITUTE_LEAVE);

        Date currentDate = dateUtil.getNewDate();

        int date = employee.getJoinDate().getDate();
        int month = employee.getJoinDate().getMonth()+1;
        int year = employee.getJoinDate().getYear()+1900;

        Date dateToGetExtraLeave = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse((year+3) + "-" + month + "-" + date);

        if (currentDate.after(dateToGetExtraLeave)){
            response.add(RequestType.EXTRA_LEAVE);
        }

        if (currentDate.getMonth() == Calendar.DECEMBER) {
            response.add(RequestType.EXTEND_ANNUAL_LEAVE);
        }

        return response;
    }

}
