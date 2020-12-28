package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.command.GetAttendancesCommand;
import com.bliblifuture.hrisbackend.command.GetFileCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AttendanceListRequest;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = AttendanceController.class)
public class AttendanceControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private AttendanceController attendanceController;

    String username = "username@mail.com";

    Principal principal;

    User user;

    @Before
    public void setup(){
        user = User.builder().username(username).build();

        principal = new Principal() {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    public void clockInTest() throws ParseException {
        ClockInClockOutRequest request = ClockInClockOutRequest.builder()
                .image("image")
                .location(LocationRequest.builder().lat(1.1).lon(1.2).build())
                .build();
        request.setRequester(principal.getName());

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00");
        AttendanceResponse attendanceResponses = AttendanceResponse.builder()
                .location(LocationResponse.builder().lat(1.1).lon(1.2).type(AttendanceLocationType.INSIDE).build())
                .image("image")
                .date(TimeResponse.builder().start(date).build())
                .build();

        Mockito.when(commandExecutor.execute(ClockInCommand.class, request))
                .thenReturn(Mono.just(attendanceResponses));

        Response<AttendanceResponse> expected = new Response<>();
        expected.setData(attendanceResponses);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        attendanceController.clockIn(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(ClockInCommand.class, request);
    }

    @Test
    public void clockOutTest() throws ParseException {
        ClockInClockOutRequest request = ClockInClockOutRequest.builder()
                .location(LocationRequest.builder().lat(1.1).lon(1.2).build())
                .build();
        request.setRequester(principal.getName());

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 18:00:00");
        AttendanceResponse attendanceResponses = AttendanceResponse.builder()
                .location(LocationResponse.builder().lat(1.1).lon(1.2).type(AttendanceLocationType.INSIDE).build())
                .image("image")
                .date(TimeResponse.builder().start(date).end(date2).build())
                .build();

        Mockito.when(commandExecutor.execute(ClockOutCommand.class, request))
                .thenReturn(Mono.just(attendanceResponses));

        Response<AttendanceResponse> expected = new Response<>();
        expected.setData(attendanceResponses);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        attendanceController.clockOut(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(ClockOutCommand.class, request);
    }

    @Test
    public void getAttendancesTests() throws ParseException {
        Date startDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-1");
        Date endDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-3");
        String start = String.valueOf(startDate.getTime());
        String end = String.valueOf(endDate.getTime());

        AttendanceListRequest request = AttendanceListRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .username(principal.getName())
                .build();

        AttendanceResponse attendance1 = AttendanceResponse.builder()
                .date(TimeResponse.builder()
                        .start(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 08:00:00"))
                        .end(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 17:00:00"))
                        .build()
                )
                .location(LocationResponse.builder()
                        .type(AttendanceLocationType.INSIDE)
                        .lon(1.1)
                        .lat(3.3)
                        .build()
                )
                .build();
        AttendanceResponse attendance2 = AttendanceResponse.builder()
                .date(TimeResponse.builder()
                        .start(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 08:12:00"))
                        .end(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-2 17:15:00"))
                        .build()
                )
                .location(LocationResponse.builder()
                        .type(AttendanceLocationType.INSIDE)
                        .lon(1.2)
                        .lat(3.4)
                        .build()
                )
                .build();
        List<AttendanceResponse> attendanceResponses = Arrays.asList(attendance1, attendance2);

        Mockito.when(commandExecutor.execute(GetAttendancesCommand.class, request))
                .thenReturn(Mono.just(attendanceResponses));

        Response<List<AttendanceResponse>> expected = new Response<>();
        expected.setData(attendanceResponses);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        attendanceController.getAttendances(start, end, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetAttendancesCommand.class, request);
    }

    @Test
    public void getImageTest() throws IOException {
        String filename = "filename";
        String request = FileConstant.IMAGE_ATTENDANCE_PATH + filename;

        byte[] fileByte = Files.readAllBytes(
                new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\image1.webp").toPath());

        Mockito.when(commandExecutor.execute(GetFileCommand.class, request))
                .thenReturn(Mono.just(fileByte));

        Response<byte[]> expected = new Response<>();
        expected.setData(fileByte);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        attendanceController.getImage(filename)
                .subscribe(response -> {
                    for (int i = 0; i < fileByte.length; i++) {
                        Assert.assertEquals(fileByte[i], response[i]);
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetFileCommand.class, request);
    }


}
