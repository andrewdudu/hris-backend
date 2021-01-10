package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetCurrentUserCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.UserResponseHelper;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetCurrentUserCommandImpl implements GetCurrentUserCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResponseHelper userResponseHelper;

    @Override
    public Mono<UserResponse> execute(String username) {
        return userRepository.findFirstByUsername(username)
                .flatMap(user -> userResponseHelper.getUserResponse(user));
    }

}
