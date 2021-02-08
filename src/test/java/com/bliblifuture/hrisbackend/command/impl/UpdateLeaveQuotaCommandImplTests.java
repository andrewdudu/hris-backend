package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.UpdateLeaveQuotaCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
public class UpdateLeaveQuotaCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public UpdateLeaveQuotaCommand updateLeaveQuotaCommand(){
            return new UpdateLeaveQuotaCommandImpl();
        }
    }

    @Autowired
    private UpdateLeaveQuotaCommand updateLeaveQuotaCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

    @Test
    public void test_execute_level12_3year() throws ParseException, IOException {
        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-01 01:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Date startOfNextYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2022-1-1 00:00:00");

        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        Mockito.when(userRepository.findAll())
                .thenReturn(Flux.just(user));

        Mockito.when(leaveRepository
                .findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear)
        )
                .thenReturn(Mono.empty());

        String id = "id1";
        Mockito.when(uuidUtil.getNewID())
                .thenReturn(id);

        Leave leave = Leave.builder()
                .remaining(10)
                .used(0)
                .expDate(startOfNextYear)
                .employeeId(user.getEmployeeId())
                .code("ANNUAL")
                .type(LeaveType.annual)
                .build();
        leave.setId(id);
        leave.setCreatedBy("SYSTEM");
        leave.setCreatedDate(currentDate);

        Mockito.when(leaveRepository.save(leave))
                .thenReturn(Mono.just(leave));

        Date joinDate = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse("2010-06-01");

        Employee employee = Employee.builder()
                .name("Employee 1")
                .level("18")
                .joinDate(joinDate)
                .build();
        employee.setId(user.getEmployeeId());

        Mockito.when(employeeRepository.findById(leave.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(leaveRepository
                .findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.extra, employee.getId(), startOfNextYear)
        )
                .thenReturn(Mono.empty());

        Leave extraLeave = Leave.builder()
                .used(0)
                .expDate(startOfNextYear)
                .employeeId(employee.getId())
                .code("EXTRA")
                .type(LeaveType.extra)
                .remaining(8)
                .build();
        extraLeave.setId(id);
        extraLeave.setCreatedBy("SYSTEM");
        extraLeave.setCreatedDate(currentDate);

        Mockito.when(leaveRepository.save(extraLeave))
                .thenReturn(Mono.just(extraLeave));

        String expected = "[SUCCESS]";

        updateLeaveQuotaCommand.execute("")
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1))
                .getNewDate();
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.extra, user.getEmployeeId(), startOfNextYear);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .save(leave);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .save(extraLeave);
        Mockito.verify(uuidUtil, Mockito.times(2)).getNewID();
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

}
