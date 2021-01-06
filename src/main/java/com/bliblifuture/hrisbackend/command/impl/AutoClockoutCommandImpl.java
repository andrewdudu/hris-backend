package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.AutoClockoutCommand;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AutoClockoutCommandImpl implements AutoClockoutCommand {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    @SneakyThrows
    public Mono<String> execute(String string) {
        Date currentTime = dateUtil.getNewDate();
        String dateString = (currentTime.getYear() + 1900) + "-" + (currentTime.getMonth() + 1) + "-" + currentTime.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);
        return attendanceRepository.findByDateAndStartTimeNotNullAndEndTimeIsNull(startOfDate)
                .flatMap(attendance -> {
                    attendance.setEndTime(currentTime);
                    attendance.setUpdatedDate(currentTime);
                    attendance.setUpdatedBy("SYSTEM");
                    return attendanceRepository.save(attendance);
                })
                .collectList()
                .map(attendances -> "[SUCCESS]");
    }

}
