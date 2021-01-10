package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendancesCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AttendanceListRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetAttendancesCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetAttendancesCommand getAttendancesCommand(){
            return new GetAttendancesCommandImpl();
        }
    }

    @Autowired
    private GetAttendancesCommand getAttendancesCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .employeeId("ID-123")
                .build();

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date startDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-1");
        Date endDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-3");

        double startLon1 = 1.2;
        double startLat1 = 3.4;
        Date startTime1 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 08:12:00");
        Date endTime1 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 17:15:00");
        Attendance attendance1 = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .locationType(AttendanceLocationType.INSIDE)
                .startLon(startLon1)
                .startLat(startLat1)
                .startTime(startTime1)
                .endTime(endTime1)
                .build();

        double startLon2 = 1.1;
        double startLat2 = 3.3;
        Date startTime2 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 08:00:00");
        Date endTime2 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 17:00:00");
        Attendance attendance2 = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .locationType(AttendanceLocationType.INSIDE)
                .startLon(startLon2)
                .startLat(startLat2)
                .startTime(startTime2)
                .endTime(endTime2)
                .build();

        Mockito.when(attendanceRepository.findByEmployeeIdAndStartTimeBetweenOrderByStartTimeDesc(user.getEmployeeId(), startDate, endDate))
                .thenReturn(Flux.just(attendance1, attendance2));

        AttendanceResponse data1 = AttendanceResponse.builder()
                .date(TimeResponse.builder()
                        .start(startTime1)
                        .end(endTime1)
                        .build()
                )
                .location(LocationResponse.builder()
                        .type(AttendanceLocationType.INSIDE)
                        .lon(startLon1)
                        .lat(startLat1)
                        .build()
                )
                .build();
        AttendanceResponse data2 = AttendanceResponse.builder()
                .date(TimeResponse.builder()
                        .start(startTime2)
                        .end(endTime2)
                        .build()
                )
                .location(LocationResponse.builder()
                        .type(AttendanceLocationType.INSIDE)
                        .lon(startLon2)
                        .lat(startLat2)
                        .build()
                )
                .build();
        List<AttendanceResponse> expected = Arrays.asList(data1, data2);

        AttendanceListRequest request = AttendanceListRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .username(user.getUsername())
                .build();
        getAttendancesCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(attendanceRepository, Mockito.times(1)).findByEmployeeIdAndStartTimeBetweenOrderByStartTimeDesc(user.getEmployeeId(), startDate, endDate);

    }
}
