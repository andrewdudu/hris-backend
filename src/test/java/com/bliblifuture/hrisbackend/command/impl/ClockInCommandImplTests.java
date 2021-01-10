package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.Office;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@RunWith(SpringRunner.class)
public class ClockInCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public ClockInCommand clockInCommand(){
            return new ClockInCommandImpl();
        }
    }

    @Autowired
    private ClockInCommand clockInCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @MockBean
    private OfficeRepository officeRepository;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

    @Test
    public void test_execute() throws ParseException, IOException {
        byte[] fileByte = Files.readAllBytes(
                new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\image1.webp").toPath());
        String base64 = "webp;" + Arrays.toString(Base64.getEncoder().encode(fileByte));

        User user = User.builder().employeeId("id123").username("username1").build();

        ClockInClockOutRequest request = ClockInClockOutRequest.builder()
                .image(base64)
                .location(LocationRequest.builder().lat(1.1).lon(1.2).build())
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 09:00:00");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-20");
        Mockito.when(attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate))
                .thenReturn(Mono.empty());

        String uuid = "uuid";
        Mockito.when(uuidUtil.getNewID()).thenReturn(uuid);

        Office office = Office.builder()
                .code("OFFICE-1")
                .name("MAIN OFFICE")
                .lon(11)
                .lat(10)
                .build();

        Mockito.when(officeRepository.findAll()).thenReturn(Flux.just(office));

        String filename = user.getEmployeeId() + "_" + currentDate.getTime() + ".webp";
        Attendance attendance = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(currentDate)
                .startLat(request.getLocation().getLat())
                .startLon(request.getLocation().getLon())
                .image(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename)
                .locationType(AttendanceLocationType.OUTSIDE)
                .build();
        attendance.setId(uuid);

        Mockito.when(attendanceRepository.save(attendance)).thenReturn(Mono.just(attendance));

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(startOfDate)
                .working(1)
                .absent(0)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DA" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(startOfDate))
                .thenReturn(Mono.empty());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        LocationResponse location = LocationResponse.builder()
                .lat(attendance.getStartLat())
                .lon(attendance.getStartLon())
                .type(attendance.getLocationType())
                .build();
        AttendanceResponse expected = AttendanceResponse.builder()
                .image(attendance.getImage())
                .location(location)
                .build();

        clockInCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(attendanceRepository, Mockito.times(1)).findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate);
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
        Mockito.verify(officeRepository, Mockito.times(1)).findAll();
        Mockito.verify(attendanceRepository, Mockito.times(1)).save(attendance);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(startOfDate);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).save(report);
    }

}
