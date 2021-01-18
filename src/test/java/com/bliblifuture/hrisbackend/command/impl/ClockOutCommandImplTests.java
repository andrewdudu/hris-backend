package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceStatus;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class ClockOutCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public ClockOutCommand clockOutCommand(){
            return new ClockOutCommandImpl();
        }
    }

    @Autowired
    private ClockOutCommand clockOutCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username1").build();

        ClockInClockOutRequest request = ClockInClockOutRequest.builder()
                .location(LocationRequest.builder().lat(1.1).lon(1.2).build())
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 18:00:00");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-20");
        String uuid = "uuid";
        String filename = user.getEmployeeId() + "_" + currentDate.getTime() + ".webp";
        Attendance attendance = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00"))
                .startLat(1.1)
                .startLon(1.2)
                .image(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename)
                .locationType(AttendanceLocationType.OUTSIDE)
                .build();
        attendance.setId(uuid);

        Mockito.when(attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate))
                .thenReturn(Mono.just(attendance));

        Attendance attendanceUpdate = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00"))
                .endTime(currentDate)
                .startLat(1.1)
                .startLon(1.2)
                .endLon(request.getLocation().getLon())
                .endLat(request.getLocation().getLat())
                .image(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename)
                .locationType(AttendanceLocationType.OUTSIDE)
                .build();
        attendance.setId(uuid);

        Mockito.when(attendanceRepository.save(attendanceUpdate)).thenReturn(Mono.just(attendanceUpdate));

        LocationResponse location = LocationResponse.builder()
                .lat(attendanceUpdate.getEndLat()).lon(attendanceUpdate.getEndLon()).build();
        AttendanceResponse expected = AttendanceResponse.builder()
                .location(location)
                .status(AttendanceStatus.FINISH)
                .build();

        clockOutCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(attendanceRepository, Mockito.times(1)).findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate);
        Mockito.verify(attendanceRepository, Mockito.times(1)).save(attendanceUpdate);
    }

}
