package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.command.GetUserDetailsByUsernameCommand;
import com.bliblifuture.hrisbackend.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetUserDetailsByUsernameCommandImpl implements GetUserDetailsByUsernameCommand {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<User> execute(String username) {
        return userRepository.findByUsername(username);
    }

}
