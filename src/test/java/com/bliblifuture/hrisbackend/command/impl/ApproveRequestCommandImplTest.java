package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.AttendanceTimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
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
public class ApproveRequestCommandImplTest {

    @TestConfiguration
    static class command{
        @Bean
        public ApproveRequestCommand approveRequestCommand(){
            return new ApproveRequestCommandImpl();
        }
    }

    @Autowired
    private ApproveRequestCommand approveRequestCommand;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private RequestResponseHelper requestResponseHelper;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

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

        AttendanceTimeResponse date = AttendanceTimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendanceResponse = AttendanceResponse.builder()
                .date(date)
                .notes(notes)
                .build();
        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendanceResponse)
                .build();

        Date currentDate = new Date();

        Request newRequest = new Request();
        BeanUtils.copyProperties(request, newRequest);
        newRequest.setStatus(RequestStatus.APPROVED);
        newRequest.setUpdatedDate(currentDate);
        newRequest.setUpdatedBy(admin.getUsername());
        newRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(newRequest)).thenReturn(Mono.just(newRequest));
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        String uuid = "ATT123";
        Mockito.when(uuidUtil.getNewID()).thenReturn(uuid);

        Attendance attendance = Attendance.builder()
                .startTime(newRequest.getClockIn())
                .endTime(newRequest.getClockOut())
                .locationType(AttendanceLocationType.REQUESTED)
                .date(newRequest.getDates().get(0))
                .build();
        attendance.setCreatedBy(newRequest.getApprovedBy());
        attendance.setCreatedDate(currentDate);
        attendance.setId(uuid);
        Mockito.when(attendanceRepository.save(attendance)).thenReturn(Mono.just(attendance));

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.ATTENDANCE)
                .date(date2)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(newRequest)).thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(newRequest);
        Mockito.verify(dateUtil, Mockito.times(2)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
        Mockito.verify(attendanceRepository, Mockito.times(1)).save(attendance);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(newRequest);
    }

}
