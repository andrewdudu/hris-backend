package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.paging.Paging;
import com.blibli.oss.common.response.Response;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.*;
import com.bliblifuture.hrisbackend.model.response.*;
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
import java.nio.file.Files;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
        RequestLeaveDetailResponse responseData = RequestLeaveDetailResponse.builder()
                .files(Arrays.asList(pathFile1, pathFile2))
                .dates(Arrays.asList(date1, date2))
                .type(type)
                .build();
        responseData.setId("id123");

        Mockito.when(commandExecutor.execute(RequestLeaveCommand.class, request))
                .thenReturn(Mono.just(responseData));

        Response<RequestLeaveDetailResponse> expected = new Response<>();
        expected.setData(responseData);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.requestLeave(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    RequestLeaveDetailResponse ex = expected.getData();
                    RequestLeaveDetailResponse res = response.getData();
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

    @Test
    public void getIncomingRequestsTest() {
        String type = "REQUESTED";
        String department = "";

        RequestResponse data1 = RequestResponse.builder()
                .user(UserResponse.builder().username("name1").build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.ATTENDANCE)
                .build();

        RequestResponse data2 = RequestResponse.builder()
                .user(UserResponse.builder().username("name2").build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.LEAVE)
                .build();

        List<RequestResponse> responseData = Arrays.asList(data1, data2);

        GetIncomingRequest request = GetIncomingRequest.builder()
                .type(type)
                .department(department)
                .page(0)
                .size(10)
                .build();
        request.setRequester(principal.getName());

        Paging paging = Paging.builder()
                .totalPage(1)
                .totalItem(2)
                .page(0)
                .itemPerPage(10)
                .build();

        PagingResponse<RequestResponse> pagingResponse = new PagingResponse<>();
        pagingResponse.setData(responseData);
        pagingResponse.setPaging(paging);

        Mockito.when(commandExecutor.execute(GetIncomingRequestCommand.class, request))
                .thenReturn(Mono.just(pagingResponse));

        Response<List<RequestResponse>> expected = new Response<>();
        expected.setData(Arrays.asList(data1, data2));
        expected.setPaging(paging);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.getIncomingRequests(type, department, 0, 10, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());

                    for (int i = 0; i < response.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i) , response.getData().get(i));
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetIncomingRequestCommand.class, request);
    }

    @Test
    public void approveRequestTest() {
        RequestResponse data = RequestResponse.builder()
                .user(UserResponse.builder().username("name1").build())
                .status(RequestStatus.APPROVED)
                .type(RequestType.ATTENDANCE)
                .build();

        String id = "id123";
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());

        Mockito.when(commandExecutor.execute(ApproveRequestCommand.class, request))
                .thenReturn(Mono.just(data));

        Response<RequestResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.approveRequest(id, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(ApproveRequestCommand.class, request);
    }

    @Test
    public void rejectRequestTest() {
        RequestResponse data = RequestResponse.builder()
                .user(UserResponse.builder().username("name1").build())
                .status(RequestStatus.REJECTED)
                .type(RequestType.ATTENDANCE)
                .build();

        String id = "id123";
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());

        Mockito.when(commandExecutor.execute(RejectRequestCommand.class, request))
                .thenReturn(Mono.just(data));

        Response<RequestResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.rejectRequest(id, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(RejectRequestCommand.class, request);
    }

    @Test
    public void bulkApproveRequest() {
        String id1 = "id1";
        String id2 = "id2";
        List<String> ids = Arrays.asList(id1, id2);
        BulkApproveRequest request = BulkApproveRequest.builder().ids(ids).build();
        request.setRequester(principal.getName());

        Mockito.when(commandExecutor.execute(BulkApproveRequestCommand.class, request))
                .thenReturn(Mono.just(BulkApproveResponse.builder().ids(ids).build()));

        Response<BulkApproveResponse> expected = new Response<>();
        expected.setData(BulkApproveResponse.builder().ids(ids).build());
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.bulkApproveRequest(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(BulkApproveRequestCommand.class, request);
    }

    @Test
    public void getImageTest() throws IOException {
        String filename = "filename";
        String request = FileConstant.REQUEST_FILE_PATH + filename;

        byte[] fileByte = Files.readAllBytes(
                new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\image1.webp").toPath());

        Mockito.when(commandExecutor.execute(GetFileCommand.class, request))
                .thenReturn(Mono.just(fileByte));

        Response<byte[]> expected = new Response<>();
        expected.setData(fileByte);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.getImage(filename)
                .subscribe(response -> {
                    for (int i = 0; i < fileByte.length; i++) {
                        Assert.assertEquals(fileByte[i], response[i]);
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetFileCommand.class, request);
    }

    @Test
    public void getPDFTest() throws IOException {
        String filename = "filename";
        String request = FileConstant.REQUEST_FILE_PATH + filename;

        byte[] fileByte = Files.readAllBytes(
                new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\file.pdf").toPath());

        Mockito.when(commandExecutor.execute(GetFileCommand.class, request))
                .thenReturn(Mono.just(fileByte));

        Response<byte[]> expected = new Response<>();
        expected.setData(fileByte);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.getPDF(filename)
                .subscribe(response -> {
                    for (int i = 0; i < fileByte.length; i++) {
                        Assert.assertEquals(fileByte[i], response[i]);
                    }
                });

        Mockito.verify(commandExecutor, Mockito.times(1)).execute(GetFileCommand.class, request);
    }

    @Test
    public void addSubstituteLeaveTest() {
        String empId = "id123";
        SubstituteLeaveRequest request = SubstituteLeaveRequest.builder()
                .id(empId)
                .total(2)
                .build();

        SubstituteLeaveRequest commandRequest = SubstituteLeaveRequest.builder()
                .id(empId)
                .total(2)
                .build();
        commandRequest.setRequester(principal.getName());

        SubstituteLeaveResponse data = SubstituteLeaveResponse
                .builder()
                .id(empId)
                .total(3)
                .build();

        Mockito.when(commandExecutor.execute(AddSubstituteLeaveCommand.class, request))
                .thenReturn(Mono.just(data));

        Response<SubstituteLeaveResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.addSubstituteLeave(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1))
                .execute(AddSubstituteLeaveCommand.class, request);
    }

    @Test
    public void requestHourlyLeaveTest() {
        String empId = "id123";
        HourlyLeaveRequest request = HourlyLeaveRequest.builder()
                .startTime("12:00")
                .endTime("13:00")
                .notes("Bisnis")
                .build();
        request.setRequester(principal.getName());

        String date = "2020-12-20";

        HourlyLeaveResponse data = HourlyLeaveResponse.builder()
                .startTime("12:00")
                .endTime("13:00")
                .notes("Bisnis")
                .dates(Arrays.asList(date))
                .build();

        Mockito.when(commandExecutor.execute(RequestHourlyLeaveCommand.class, request))
                .thenReturn(Mono.just(data));

        Response<HourlyLeaveResponse> expected = new Response<>();
        expected.setData(data);
        expected.setCode(HttpStatus.OK.value());
        expected.setStatus(HttpStatus.OK.name());

        requestController.requestHourlyLeave(request, principal)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getCode(), response.getCode());
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getData(), response.getData());
                });

        Mockito.verify(commandExecutor, Mockito.times(1))
                .execute(RequestHourlyLeaveCommand.class, request);
    }

}
