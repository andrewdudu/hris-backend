package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetIncomingRequestsCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetIncomingRequestCommand getIncomingRequestCommand(){
            return new GetIncomingRequestCommandImpl();
        }
    }

    @Autowired
    private GetIncomingRequestCommand getIncomingRequestCommand;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private RequestResponseHelper requestResponseHelper;

    @Test
    public void test_execute() throws ParseException {
        User user1 = User.builder().employeeId("id123").username("username1").build();
        User user2 = User.builder().employeeId("id456").username("username2").build();

        String type = "REQUESTED";

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-21");

        Date date3 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        String notes1 = "notes1";
        String notes2 = "notes2";
        Request request1 = Request.builder()
                .employeeId(user1.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .notes(notes1)
                .build();
        request1.setCreatedDate(date3);

        Request request2 = Request.builder()
                .employeeId(user2.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date2))
                .status(RequestStatus.REQUESTED)
                .notes(notes2)
                .build();
        request2.setCreatedDate(date3);

        Mockito.when(requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(type)))
                .thenReturn(Flux.just(request1, request2));

        UserResponse userResponse1 = UserResponse.builder()
                .username(user1.getUsername())
                .employeeId(user1.getEmployeeId())
                .build();

        UserResponse userResponse2 = UserResponse.builder()
                .username(user2.getUsername())
                .employeeId(user2.getEmployeeId())
                .build();

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(notes1)
                .build();
        RequestDetailResponse detail1 = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();
        IncomingRequestResponse data1 = IncomingRequestResponse.builder()
                .user(UserResponse.builder().username(user1.getUsername()).employeeId(user1.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.ATTENDANCE)
                .date(date3)
                .detail(detail1)
                .build();

        RequestLeaveResponse leave = RequestLeaveResponse.builder()
                .dates(Collections.singletonList("2020-10-21"))
                .notes(notes2)
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        RequestDetailResponse detail2 = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        IncomingRequestResponse data2 = IncomingRequestResponse.builder()
                .user(UserResponse.builder().username(user2.getUsername()).employeeId(user2.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.LEAVE)
                .detail(detail2)
                .date(date3)
                .build();

        Mockito.when(requestResponseHelper.createResponse(request1)).thenReturn(Mono.just(data1));
        Mockito.when(requestResponseHelper.createResponse(request2)).thenReturn(Mono.just(data2));

        List<IncomingRequestResponse> expected = Arrays.asList(data1, data2);

        GetIncomingRequest request = GetIncomingRequest.builder().type(type).build();
        request.setRequester("admin");

        getIncomingRequestCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request1);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request2);
        Mockito.verify(requestRepository, Mockito.times(1)).findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(type));
    }

}
