package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.UserResponseHelper;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.LoginResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.Arrays;

@RunWith(SpringRunner.class)
public class LoginCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public LoginCommand loginCommand(){
            return new LoginCommandImpl();
        }
    }

    @Autowired
    private LoginCommand loginCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserResponseHelper userResponseHelper;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        LoginRequest request = LoginRequest.builder()
                .username(user.getUsername())
                .password("password")
                .build();

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(true);

        String token = "token";
        Mockito.when(jwtTokenUtil.generateToken(user))
                .thenReturn(token);

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
        Mockito.when(userResponseHelper.getUserResponse(user))
                .thenReturn(Mono.just(userResponse));

        LoginResponse expected = LoginResponse.builder()
                .accessToken(token)
                .userResponse(userResponse)
                .build();

        loginCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(passwordEncoder, Mockito.times(1)).matches(request.getPassword(), user.getPassword());
        Mockito.verify(jwtTokenUtil, Mockito.times(1)).generateToken(user);
        Mockito.verify(userResponseHelper, Mockito.times(1)).getUserResponse(user);
    }
}
