package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCurrentUserCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.UserResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.PositionResponse;
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
import java.util.Arrays;

@RunWith(SpringRunner.class)
public class GetCurrentUserCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetCurrentUserCommand getCurrentUserCommand(){
            return new GetCurrentUserCommandImpl();
        }
    }

    @Autowired
    private GetCurrentUserCommand getCurrentUserCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserResponseHelper userResponseHelper;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .password("pass")
                .build();

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        UserResponse expected = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .position(PositionResponse.builder().name("Staff IT").build())
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .name("name")
                .department("DEP-1")
                .roles(user.getRoles())
                .office(OfficeResponse.builder().name("MAIN OFFICE").build())
                .leave(LeaveResponse.builder().remaining(10).used(2).build())
                .build();

        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(expected));

        getCurrentUserCommand.execute(user.getUsername())
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);

    }
}
