package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.UpdateAnnualLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UpdateAnnualLeaveCommandImpl implements UpdateAnnualLeaveCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<String> execute(String string) {
        return userRepository.findAll()
                .flatMap(this::createNewAnnualLeave)
                .flatMap(leave -> leaveRepository.save(leave))
                .collectList()
                .map(leaves -> "[SUCCESS]");
    }

    @SneakyThrows
    private Mono<Leave> createNewAnnualLeave(User user) {
        Date date = dateUtil.getNewDate();
        Date endOfTheYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse((date.getYear()+1900) + "-12-31 23:59:59");
        return leaveRepository.findFirstByEmployeeIdAndExpDate(user.getEmployeeId(), endOfTheYear)
                .switchIfEmpty(Mono.just(createLeave(endOfTheYear, user.getEmployeeId(), date)));
    }

    private Leave createLeave(Date endOfTheYear, String employeeId, Date currentDate) {
        Leave leave = Leave.builder()
                .remaining(10)
                .used(0)
                .expDate(endOfTheYear)
                .employeeId(employeeId)
                .code("ANNUAL")
                .type(LeaveType.annual)
                .build();
        leave.setId(uuidUtil.getNewID());
        leave.setCreatedBy("SYSTEM");
        leave.setCreatedDate(currentDate);

        return leave;
    }

}
