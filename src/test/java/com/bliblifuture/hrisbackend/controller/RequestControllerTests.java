package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.GetExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.command.RequestExtendLeaveCommand;
import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequestData;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.AttendanceRequestResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import com.bliblifuture.hrisbackend.model.response.util.ExtendLeaveQuotaResponse;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.apache.commons.io.FileUtils;
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
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = RequestController.class)
public class RequestControllerTests {

    @MockBean
    private CommandExecutor commandExecutor;

    @Autowired
    private RequestController requestController;

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
    public void requestAttendanceTest(){
        String startTime = "08:00";
        String endTime = "17:00";
        String date = "2020-05-25";
        String notes = "notes";
        AttendanceRequestData request = AttendanceRequestData.builder()
                .ClockIn(startTime)
                .ClockOut(endTime)
                .date(date)
                .notes(notes)
                .build();

        AttendanceRequestResponse responseData = AttendanceRequestResponse.builder()
                .ClockIn(startTime)
                .ClockOut(endTime)
                .date(date)
                .notes(notes)
                .build();
        responseData.setId("id123");

        Mockito.when(commandExecutor.execute(RequestAttendanceCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<AttendanceRequestResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.requestAttendances(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    AttendanceRequestResponse ex = expected.getData();
                    AttendanceRequestResponse res = response.getData();
                    Assert.assertEquals(ex.getId(), res.getId());
                    Assert.assertEquals(ex.getClockIn(), res.getClockIn());
                    Assert.assertEquals(ex.getClockOut(), res.getClockOut());
                    Assert.assertEquals(ex.getDate(), res.getDate());
                    Assert.assertEquals(ex.getNotes(), res.getNotes());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(RequestAttendanceCommand.class, request);
    }

    @Test
    public void requestLeaveTest() throws IOException {
        String date1 = "2020-05-25";
        String date2 = "2020-05-26";
        String type = "SICK_WITH_MEDICAL_LETTER";

        String image1 = "image1.webp";
        String image2 = "image2.webp";

        byte[] file1 = FileUtils.readFileToByteArray(new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\" + image1));
        String file1Base64 = Base64.getEncoder().encodeToString(file1);
        byte[] file2 = FileUtils.readFileToByteArray(new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\" + image2));
        String file2Base64 = Base64.getEncoder().encodeToString(file2);

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(date1, date2))
                .files(Arrays.asList(file1Base64, file2Base64))
                .type(type)
                .build();

        String pathFile1 = FileConstant.REQUEST_FILE_BASE_URL + image1;
        String pathFile2 = FileConstant.REQUEST_FILE_BASE_URL + image2;
        LeaveRequestResponse responseData = LeaveRequestResponse.builder()
                .files(Arrays.asList(pathFile1, pathFile2))
                .dates(Arrays.asList(date1, date2))
                .type(type)
                .build();
        responseData.setId("id123");

        Mockito.when(commandExecutor.execute(RequestLeaveCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<LeaveRequestResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.requestLeave(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    LeaveRequestResponse ex = expected.getData();
                    LeaveRequestResponse res = response.getData();
                    Assert.assertEquals(ex.getId(), res.getId());
                    Assert.assertEquals(ex.getFiles(), res.getFiles());
                    Assert.assertEquals(ex.getDates(), res.getDates());
                    Assert.assertEquals(ex.getType(), res.getType());
                    Assert.assertEquals(ex.getNotes(), res.getNotes());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(RequestLeaveCommand.class, request);
    }

    @Test
    public void getExtendLeaveDataTest() throws ParseException {
        String dateString = "2020-05-25";
        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
        System.out.println(date.getTime());

        ExtendLeaveQuotaResponse quota = ExtendLeaveQuotaResponse.builder()
                .remaining(3)
                .extensionDate(date)
                .build();

        ExtendLeaveResponse responseData = ExtendLeaveResponse.builder()
                .status(RequestStatus.AVAILABLE)
                .quota(quota)
                .build();

        Mockito.when(commandExecutor.execute(GetExtendLeaveDataCommand.class, user.getUsername()))
                .thenReturn(Mono.just(responseData));

        Response<ExtendLeaveResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.getExtendLeaveData(principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    ExtendLeaveResponse ex = expected.getData();
                    ExtendLeaveResponse res = response.getData();
                    Assert.assertEquals(ex.getStatus(), res.getStatus());
                    Assert.assertEquals(ex.getQuota(), res.getQuota());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetExtendLeaveDataCommand.class, user.getUsername());
    }

    @Test
    public void requestExtendLeaveTest() {
        String notes = "notes";
        ExtendLeaveResponse responseData = ExtendLeaveResponse.builder()
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();

        LeaveRequestData request = LeaveRequestData.builder()
                .notes(notes)
                .build();

        Mockito.when(commandExecutor.execute(RequestExtendLeaveCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<ExtendLeaveResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.requestExtendLeave(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    ExtendLeaveResponse ex = expected.getData();
                    ExtendLeaveResponse res = response.getData();
                    Assert.assertEquals(ex.getNotes(), res.getNotes());
                    Assert.assertEquals(ex.getStatus(), res.getStatus());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(RequestExtendLeaveCommand.class, request);
    }


}
