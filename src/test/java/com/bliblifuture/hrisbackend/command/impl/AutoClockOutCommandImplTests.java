package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.AutoClockoutCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class AutoClockOutCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public AutoClockoutCommand autoClockoutCommand(){
            return new AutoClockoutCommandImpl();
        }
    }

    @Autowired
    private AutoClockoutCommand autoClockoutCommand;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException, IOException {
        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2021-01-10 23:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-01-10");

        String empId = "id123";
        Date startTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2021-01-10 09:00:00");

        Attendance attendance1 = Attendance.builder()
                .employeeId(empId)
                .locationType(AttendanceLocationType.INSIDE)
                .officeCode("OFFICE-1")
                .startLon(1.1)
                .startLat(1.2)
                .startTime(startTime)
                .date(startOfDate)
                .build();

        Mockito.when(attendanceRepository.findByDateAndStartTimeNotNullAndEndTimeIsNull(startOfDate))
                .thenReturn(Flux.just(attendance1));

        Mockito.when(attendanceRepository.save(attendance1))
                .thenReturn(Mono.just(attendance1));

        String expected = "[SUCCESS]";

        autoClockoutCommand.execute("")
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(attendanceRepository, Mockito.times(1))
                .findByDateAndStartTimeNotNullAndEndTimeIsNull(startOfDate);
    }

}
