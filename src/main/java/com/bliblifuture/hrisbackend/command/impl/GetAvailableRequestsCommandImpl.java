package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    @SneakyThrows
    public Mono<List<RequestType>> execute(String username) {
        Date currentDate = dateUtil.getNewDate();
        Date startOfNextYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse((currentDate.getYear()+1901) + "-1-1 00:00:00");
        return employeeRepository.findFirstByEmail(username)
                .map(employee -> getResponse(employee, currentDate))
                .flatMap(response -> userRepository.findFirstByUsername(username)
                        .flatMap(user -> leaveRepository.findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear)
                                .switchIfEmpty(Mono.just(Leave.builder().build()))
                                .map(leave -> checkExtendLeave(leave, response, currentDate))
                                .map(res -> addExtraList(user, res))
                        )
                );
    }

    private List<RequestType> checkExtendLeave(Leave leave, List<RequestType> response, Date currentDate) {
        if (leave.getId() == null || leave.getId().isEmpty()){
            return response;
        }
        if (currentDate.getMonth() == Calendar.DECEMBER) {
            response.add(RequestType.EXTEND_ANNUAL_LEAVE);
        }
        return response;
    }

    private List<RequestType> addExtraList(User user, List<RequestType> response) {
        if (user.getRoles().contains(UserRole.MANAGER)){
            response.add(RequestType.INCOMING_REQUESTS);
        }
        if (user.getRoles().contains(UserRole.ADMIN)){
            response.add(RequestType.INCOMING_REQUESTS);
            response.add(RequestType.SET_HOLIDAY);
            response.add(RequestType.EMPLOYEE);
            response.add(RequestType.ADD_ANNOUNCEMENT);
        }
        return response;
    }

    @SneakyThrows
    private List<RequestType> getResponse(Employee employee, Date currentDate){
        List<RequestType> response = new ArrayList<>();
        response.add(RequestType.ATTENDANCE);
        response.add(RequestType.ANNUAL_LEAVE);
        response.add(RequestType.SPECIAL_LEAVE);
        response.add(RequestType.SUBSTITUTE_LEAVE);
        response.add(RequestType.HOURLY_LEAVE);

        int date = employee.getJoinDate().getDate();
        int month = employee.getJoinDate().getMonth()+1;
        int year = employee.getJoinDate().getYear()+1900;

        Date dateToGetExtraLeave = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse((year+3) + "-" + month + "-" + date);;

        if (currentDate.after(dateToGetExtraLeave)){
            response.add(RequestType.EXTRA_LEAVE);
        }

        return response;
    }

}
