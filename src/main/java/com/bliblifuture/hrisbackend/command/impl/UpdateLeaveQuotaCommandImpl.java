package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.UpdateLeaveQuotaCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UpdateLeaveQuotaCommandImpl implements UpdateLeaveQuotaCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    @SneakyThrows
    public Mono<String> execute(String string) {
        Date currentDate = dateUtil.getNewDate();

        String dateString = (currentDate.getYear()+1897) + "-" + (currentDate.getMonth()+1) + "-"
                + currentDate.getDate();
        String startTime = " 00:00:00";
        Date availableExtraLeaveJoinDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        Date startOfNextYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse((currentDate.getYear()+1901) + "-1-1 00:00:00");

        return userRepository.findAll()
                .collectList()
                .flatMap(users -> Flux.fromIterable(users)
                        .flatMap(user -> createNewAnnualLeave(user, currentDate, startOfNextYear)
                                .flatMap(leave -> leaveRepository.save(leave))
                                .flatMap(leave -> employeeRepository.findById(leave.getEmployeeId()))
                                .filter(employee -> employee.getJoinDate().before(availableExtraLeaveJoinDate) && employee.getLevel() != null)
                                .flatMap(employee -> createNewExtraLeave(employee, currentDate, startOfNextYear))
                                .flatMap(extraLeave -> leaveRepository.save(extraLeave))
                        )
                        .collectList()
                )
                .map(leaves -> "[SUCCESS]");
    }

    private Mono<Leave> createNewExtraLeave(Employee employee, Date currentDate, Date startOfNextYear) {
        return leaveRepository.findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.extra, employee.getId(), startOfNextYear)
                .switchIfEmpty(Mono.just(createExtraLeave(startOfNextYear, employee, currentDate)));
    }

    private Mono<Leave> createNewAnnualLeave(User user, Date currentDate, Date startOfNextYear) {
        return leaveRepository.findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear)
                .switchIfEmpty(Mono.just(createAnnualLeave(startOfNextYear, user.getEmployeeId(), currentDate)));
    }

    @SneakyThrows
    private Leave createExtraLeave(Date startOfNextYear, Employee employee, Date currentDate) {
        Leave leave = Leave.builder()
                .used(0)
                .expDate(startOfNextYear)
                .employeeId(employee.getId())
                .code("EXTRA")
                .type(LeaveType.extra)
                .build();
        leave.setId(uuidUtil.getNewID());
        leave.setCreatedBy("SYSTEM");
        leave.setCreatedDate(currentDate);

        int level = Integer.parseInt(employee.getLevel());
        Date joinDate = employee.getJoinDate();

        int extra = 0;
        if (level > 11 && level <= 14){
            extra = 1;
        }
        else if(level > 14 && level <= 16){
            extra = 2;
        }
        else if(level > 16 && level <= 18){
            extra = 3;
        }

        int year = currentDate.getYear();
        int month = currentDate.getMonth() + 1;
        int date = currentDate.getDate();

        Date fourYearsWorkingTimeJoinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse((year+1896) + "-" + month + "-" + date);

        Date sixYearsWorkingTimeJoinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse((year+1894) + "-" + month + "-" + date);

        Date eightYearsWorkingTimeJoinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse((year+1892) + "-" + month + "-" + date);

        Date tenYearsWorkingTimeJoinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse((year+1890) + "-" + month + "-" + date);

        if (joinDate.after(fourYearsWorkingTimeJoinDate)){
            leave.setRemaining(2 + extra);
        }
        else if (joinDate.after(sixYearsWorkingTimeJoinDate)){
            leave.setRemaining(3 + extra);
        }
        else if (joinDate.after(eightYearsWorkingTimeJoinDate)){
            leave.setRemaining(4 + extra);
        }
        else {
            leave.setRemaining(5 + extra);
        }

        return leave;
    }

    private Leave createAnnualLeave(Date startOfNextYear, String employeeId, Date currentDate) {
        Leave leave = Leave.builder()
                .remaining(10)
                .used(0)
                .expDate(startOfNextYear)
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
