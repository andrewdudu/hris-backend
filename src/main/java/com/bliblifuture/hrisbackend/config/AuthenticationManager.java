package com.bliblifuture.hrisbackend.config;

import com.blibli.oss.command.CommandExecutor;
import com.bliblifuture.hrisbackend.command.GetUserDetailsByUsernameCommand;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    CommandExecutor commandExecutor;

    @Autowired
    private UserRepository userRepository;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication.getCredentials() != null){
            String token = authentication.getCredentials().toString();
            if (jwtTokenUtil.validateToken(token)){
                String username = jwtTokenUtil.getUsernameFromToken(token);
                return commandExecutor.execute(GetUserDetailsByUsernameCommand.class, username)
                        .flatMap(user -> isValid(token, user));
            }
        }
        return Mono.empty();
    }

    public Mono<Authentication> isValid(String token, User user){
        if(true){
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return Mono.just(new UsernamePasswordAuthenticationToken(jwtTokenUtil.getUsernameFromToken(token), null, authorities));
        }
        return Mono.empty();
    }
}
