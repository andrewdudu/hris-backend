package com.bliblifuture.hrisbackend.util;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateUtil {

    public Date getNewDate(){
        return new Date();
    }
}
