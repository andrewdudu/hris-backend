package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.repository.*;
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
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @MockBean
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

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

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

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
        AttendanceResponse attendanceResponse = AttendanceResponse.builder()
                .date(date)
                .notes(notes)
                .build();
        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendanceResponse)
                .build();

        String dateString = (currentDate.getYear() + 1900) + "-" + (currentDate.getMonth() + 1) + "-" + currentDate.getDate();
        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        Mockito.when(dailyAttendanceReportRepository.findByDate(startOfDate))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(startOfDate)
                .working(1)
                .absent(0)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        String uuid = "ATT123";
        Mockito.when(uuidUtil.getNewID()).thenReturn(uuid);

        Attendance attendance = Attendance.builder()
                .startTime(approvedRequest.getClockIn())
                .endTime(approvedRequest.getClockOut())
                .locationType(AttendanceLocationType.REQUESTED)
                .date(approvedRequest.getDates().get(0))
                .employeeId(user.getEmployeeId())
                .build();
        attendance.setCreatedBy(approvedRequest.getApprovedBy());
        attendance.setCreatedDate(currentDate);
        attendance.setId(uuid);

        Mockito.when(attendanceRepository.save(attendance))
                .thenReturn(Mono.just(attendance));

        IncomingRequestResponse expected = IncomingRequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.ATTENDANCE)
                .date(date2)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
        Mockito.verify(attendanceRepository, Mockito.times(1)).save(attendance);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(approvedRequest);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).findByDate(startOfDate);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).save(report);

    }

}
