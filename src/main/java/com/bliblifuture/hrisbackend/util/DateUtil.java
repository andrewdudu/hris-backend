package com.bliblifuture.hrisbackend.util;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateUtil {

    public final static String DATE_FORMAT = "yyyy-MM-dd";

    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public Date getNewDate(){
        return new Date();
    }

}
