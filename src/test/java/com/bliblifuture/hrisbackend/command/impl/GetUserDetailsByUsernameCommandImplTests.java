package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetUserDetailsByUsernameCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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

import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringRunner.class)
public class GetUserDetailsByUsernameCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetUserDetailsByUsernameCommand getUserDetailsByUsernameCommand(){
            return new GetUserDetailsByUsernameCommandImpl();
        }
    }

    @Autowired
    private GetUserDetailsByUsernameCommand getUserDetailsByUsernameCommand;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void test_execute() throws IOException {
        String username = "username";

        User expected = User.builder()
                .username(username)
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .employeeId("emp-123")
                .build();

        Mockito.when(userRepository.findByUsername(username))
                .thenReturn(Mono.just(expected));

        getUserDetailsByUsernameCommand.execute(username)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });
    }
}
