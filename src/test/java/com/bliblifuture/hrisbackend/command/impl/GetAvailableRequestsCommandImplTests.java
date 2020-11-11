package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableRequestsCommand;
import com.bliblifuture.hrisbackend.constant.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
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
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {

        User user = User.builder().username("username").build();

        Date currentDate = new SimpleDateFormat("dd/MM/yy").parse("11/12/2020");

        Date joinDate = new SimpleDateFormat("dd/MM/yy").parse("10/10/2017");

        Employee employee = Employee.builder()
                .name("name")
                .email(user.getUsername())
                .joinDate(joinDate)
                .build();

        List<RequestType> expected = new ArrayList<>();
        expected.add(RequestType.ATTENDANCE);
        expected.add(RequestType.ANNUAL_LEAVE);
        expected.add(RequestType.SPECIAL_LEAVE);
        expected.add(RequestType.SUBTITUTE_LEAVE);
        expected.add(RequestType.EXTRA_LEAVE);
        expected.add(RequestType.EXTEND_ANNUAL_LEAVE);

        Mockito.when(employeeRepository.findByEmail(user.getUsername()))
                .thenReturn(Mono.just(employee));
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        getAvailableRequestsCommand.execute(user.getUsername())
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(user.getUsername());
    }

}
