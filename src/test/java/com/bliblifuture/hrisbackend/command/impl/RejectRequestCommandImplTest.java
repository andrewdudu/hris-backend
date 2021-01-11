package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RejectRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@RunWith(SpringRunner.class)
public class RejectRequestCommandImplTest {

    @TestConfiguration
    static class command{
        @Bean
        public RejectRequestCommand rejectRequestCommand(){
            return new RejectRequestCommandImpl();
        }
    }

    @Autowired
    private RejectRequestCommand rejectRequestCommand;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private RequestResponseHelper requestResponseHelper;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        String notes = "notes";
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();
        request.setCreatedDate(date2);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(notes)
                .build();
        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();

        Date currentDate = new Date();

        Request newRequest = new Request();
        BeanUtils.copyProperties(request, newRequest);
        newRequest.setStatus(RequestStatus.REJECTED);
        newRequest.setUpdatedDate(currentDate);
        newRequest.setUpdatedBy(admin.getUsername());
        newRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(newRequest)).thenReturn(Mono.just(newRequest));
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        RequestResponse expeted = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.REJECTED)
                .type(RequestType.ATTENDANCE)
                .date(date2)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(newRequest)).thenReturn(Mono.just(expeted));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        rejectRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expeted, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(newRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(newRequest);
    }

}
