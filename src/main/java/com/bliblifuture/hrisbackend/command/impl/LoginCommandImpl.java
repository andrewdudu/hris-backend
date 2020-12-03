package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.LoginCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.UserResponseHelper;
import com.bliblifuture.hrisbackend.config.JwtTokenUtil;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LoginRequest;
import com.bliblifuture.hrisbackend.model.response.LoginResponse;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginCommandImpl implements LoginCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserResponseHelper userResponseHelper;

    @Override
    public Mono<LoginResponse> execute(LoginRequest request) {
        return Mono.fromCallable(request::getUsername)
                .flatMap(username -> userRepository.findByUsername(username)
                        .doOnSuccess(this::checkNull)
                        .flatMap(user -> authenticateAndGetResponse(user, request))
                );
    }

    private void checkNull(User user) {
        if (user == null){
            Map<String, List<String>> errors = new HashMap<>();
            errors.put("credentials", Arrays.asList("DOES_NOT_MATCH, TEST"));
            String errorsMessage = errors.toString();
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Mono<LoginResponse> authenticateAndGetResponse(User user, LoginRequest request) {
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())){
            String token = jwtTokenUtil.generateToken(user);
            LoginResponse response = LoginResponse.builder()
                    .accessToken(token).build();
            return userResponseHelper.getUserResponse(user)
                    .map(userResponse -> {
                        response.setUserResponse(userResponse);
                        return response;
                    });
        }
        else{
            throw new IllegalArgumentException("DOES_NOT_MATCH");
        }
    }

}
