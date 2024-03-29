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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetAvailableRequestsCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetAvailableRequestsCommand getAvailableRequestsCommand(){
            return new GetAvailableRequestsCommandImpl();
        }
    }

    @Autowired
    private GetAvailableRequestsCommand getAvailableRequestsCommand;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {

        User user = User.builder().username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN)).build();

        Date currentDate = new SimpleDateFormat("dd/MM/yyyy").parse("11/12/2020");

        Date joinDate = new SimpleDateFormat("dd/MM/yyyy").parse("10/10/2017");

        Employee employee = Employee.builder()
                .name("name")
                .email(user.getUsername())
                .joinDate(joinDate)
                .build();

        List<RequestType> expected = new ArrayList<>();
        expected.add(RequestType.ATTENDANCE);
        expected.add(RequestType.ANNUAL_LEAVE);
        expected.add(RequestType.SPECIAL_LEAVE);
        expected.add(RequestType.SUBSTITUTE_LEAVE);
        expected.add(RequestType.HOURLY_LEAVE);
        expected.add(RequestType.EXTRA_LEAVE);
        expected.add(RequestType.EXTEND_ANNUAL_LEAVE);
        expected.add(RequestType.INCOMING_REQUESTS);
        expected.add(RequestType.SET_HOLIDAY);
        expected.add(RequestType.EMPLOYEE);

        Mockito.when(employeeRepository.findFirstByEmail(user.getUsername()))
                .thenReturn(Mono.just(employee));
        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date startOfNextYear = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-1-1 00:00:00");

        Leave annual = Leave.builder()
                .employeeId(user.getEmployeeId())
                .type(LeaveType.annual)
                .expDate(startOfNextYear)
                .remaining(10)
                .used(2)
                .build();
        annual.setId("Annual-123");

        Mockito.when(leaveRepository.findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear))
                .thenReturn(Mono.just(annual));

        getAvailableRequestsCommand.execute(user.getUsername())
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(employeeRepository, Mockito.times(1)).findFirstByEmail(user.getUsername());
        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1)).findFirstByTypeAndEmployeeIdAndExpDate(LeaveType.annual, user.getEmployeeId(), startOfNextYear);
    }

}
