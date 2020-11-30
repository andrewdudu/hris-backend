package com.bliblifuture.hrisbackend.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidUtil {

    public String getNewID(){
        return UUID.randomUUID().toString();
    }

}
