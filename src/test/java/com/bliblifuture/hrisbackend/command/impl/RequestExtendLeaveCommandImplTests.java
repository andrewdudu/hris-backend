package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestExtendLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class RequestExtendLeaveCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public RequestExtendLeaveCommand requestExtendLeaveCommand(){
            return new RequestExtendLeaveCommandImpl();
        }
    }

    @Autowired
    private RequestExtendLeaveCommand requestExtendLeaveCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 10:00:00");
        String id = "REQ_EXT-" + user.getEmployeeId() + "-" + currentDate.getTime();

        String notes = "notes";
        LeaveRequestData request = LeaveRequestData.builder()
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);
        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Request entity = Request.builder()
                .status(RequestStatus.REQUESTED)
                .type(RequestType.EXTEND_ANNUAL_LEAVE)
                .notes(notes)
                .employeeId(user.getEmployeeId())
                .build();
        entity.setId(id);

        Mockito.when(requestRepository.save(entity))
                .thenReturn(Mono.just(entity));

        Mockito.when(requestRepository.findFirstByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED))
                .thenReturn(Mono.empty());

        ExtendLeaveResponse expected = ExtendLeaveResponse.builder()
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();

        requestExtendLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(1)).save(entity);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findFirstByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED);
    }

    @Test
    public void test_execute_unavailable() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-11-20 10:00:00");

        String notes = "notes";
        LeaveRequestData request = LeaveRequestData.builder()
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        ExtendLeaveResponse expected = ExtendLeaveResponse.builder()
                .status(RequestStatus.UNAVAILABLE)
                .notes(notes)
                .build();

        requestExtendLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(0)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(requestRepository, Mockito.times(0))
                .findFirstByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED);
    }

    @Test(expected = Exception.class)
    public void test_execute_requestedException() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 10:00:00");
        String id = "REQ_EXT-" + user.getEmployeeId() + "-" + currentDate.getTime();

        String notes = "notes";
        LeaveRequestData request = LeaveRequestData.builder()
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);
        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Request entity = Request.builder()
                .status(RequestStatus.REQUESTED)
                .type(RequestType.EXTEND_ANNUAL_LEAVE)
                .notes(notes)
                .employeeId(user.getEmployeeId())
                .build();
        entity.setId(id);

        Mockito.when(requestRepository.findFirstByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED))
                .thenReturn(Mono.just(entity));

        ExtendLeaveResponse expected = ExtendLeaveResponse.builder()
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();

        requestExtendLeaveCommand.execute(request).subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(0)).save(entity);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findFirstByEmployeeIdAndTypeAndStatus(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, RequestStatus.REQUESTED);
    }

}
