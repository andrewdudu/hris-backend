package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestAttendanceCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.AttendanceRequest;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequestData;
import com.bliblifuture.hrisbackend.model.response.AttendanceRequestResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRequestRepository;
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
public class RequestAttendanceCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public RequestAttendanceCommand requestAttendanceCommand(){
            return new RequestAttendanceCommandImpl();
        }
    }

    @Autowired
    private RequestAttendanceCommand requestAttendanceCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AttendanceRequestRepository attendanceRequestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        String startTime = "08:00";
        String endTime = "17:00";
        String dateString = "2020-05-25";
        String notes = "notes";

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "REQ_ATT-" + user.getEmployeeId() + "-" + currentDate.getTime();

        AttendanceRequestData request = AttendanceRequestData.builder()
                .ClockIn(startTime)
                .ClockOut(endTime)
                .date(dateString)
                .notes(notes)
                .build();
        request.setRequester(user.getUsername());

        Date clockIn = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse(dateString + " " + startTime + ":00");
        Date clockOut = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse(dateString + " " + endTime + ":00");
        Date date = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString);
        AttendanceRequest entity = AttendanceRequest.builder()
                .clockIn(clockIn)
                .clockOut(clockOut)
                .date(date)
                .notes(notes)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .build();
        entity.setId(id);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));
        Mockito.when(attendanceRequestRepository.save(entity))
                .thenReturn(Mono.just(entity));

        AttendanceRequestResponse expected = AttendanceRequestResponse.builder()
                .ClockIn(startTime)
                .ClockOut(endTime)
                .date(dateString)
                .notes(notes)
                .build();
        expected.setId(id);

        requestAttendanceCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getDate(), response.getDate());
                    Assert.assertEquals(expected.getClockIn(), response.getClockIn());
                    Assert.assertEquals(expected.getClockOut(), response.getClockOut());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(attendanceRequestRepository, Mockito.times(1)).save(entity);
    }

}
