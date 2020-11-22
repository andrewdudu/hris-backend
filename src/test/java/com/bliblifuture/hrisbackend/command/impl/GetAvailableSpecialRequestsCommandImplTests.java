package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAvailableSpecialRequestsCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetAvailableSpecialRequestsCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetAvailableSpecialRequestsCommand getAvailableSpecialRequestsCommand(){
            return new GetAvailableSpecialRequestsCommandImpl();
        }
    }

    @Autowired
    private GetAvailableSpecialRequestsCommand getAvailableSpecialRequestsCommand;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    public void test_execute() {

        User user = User.builder().username("username").build();

        Employee employee = Employee.builder()
                .name("name")
                .email(user.getUsername())
                .build();

        List<SpecialLeaveType> expected = new ArrayList<>();
        expected.add(SpecialLeaveType.SICK);
        expected.add(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER);
        expected.add(SpecialLeaveType.MARRIAGE);
        expected.add(SpecialLeaveType.MATERNITY);
        expected.add(SpecialLeaveType.CHILDBIRTH);
        expected.add(SpecialLeaveType.MAIN_FAMILY_DEATH);
        expected.add(SpecialLeaveType.CLOSE_FAMILY_DEATH);
        expected.add(SpecialLeaveType.HAJJ);
        expected.add(SpecialLeaveType.CHILD_BAPTISM);
        expected.add(SpecialLeaveType.CHILD_CIRCUMSION);
        expected.add(SpecialLeaveType.UNPAID_LEAVE);

        Mockito.when(employeeRepository.findByEmail(user.getUsername()))
                .thenReturn(Mono.just(employee));

        getAvailableSpecialRequestsCommand.execute(user.getUsername())
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(user.getUsername());
    }

}
