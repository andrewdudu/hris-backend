package com.bliblifuture.hrisbackend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PassEncoder extends BCryptPasswordEncoder{

}
