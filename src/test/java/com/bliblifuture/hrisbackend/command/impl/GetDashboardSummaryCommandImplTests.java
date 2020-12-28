package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
public class GetDashboardSummaryCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetDashboardSummaryCommand getDashboardSummaryCommand(){
            return new GetDashboardSummaryCommandImpl();
        }
    }

    @Autowired
    private GetDashboardSummaryCommand getDashboardSummaryCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-12 09:00:00");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");
        Mockito.when(dailyAttendanceReportRepository.findByDate(startOfDate))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(startOfDate)
                .working(0)
                .absent(0)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Mockito.when(eventRepository.findByDate(startOfDate))
                .thenReturn(Mono.empty());

        long requestCount = 10L;
        Mockito.when(requestRepository.countByStatus(RequestStatus.REQUESTED))
                .thenReturn(Mono.just(requestCount));

        Pageable pageable = PageRequest.of(0, 2);

        Attendance attendance1 = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-11"))
                .locationType(AttendanceLocationType.INSIDE)
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-11 08:00:00"))
                .endTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-11 18:00:00"))
                .startLat(3.1)
                .startLon(1.5)
                .endLat(3.2)
                .endLon(1.6)
                .officeCode("OFFICE-1")
                .build();

        Attendance attendance2 = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12"))
                .locationType(AttendanceLocationType.OUTSIDE)
                .image("imagePath")
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-12 08:00:00"))
                .startLat(3.1)
                .startLon(1.5)
                .officeCode("OFFICE-1")
                .build();

        Mockito.when(attendanceRepository.findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable))
                .thenReturn(Flux.just(attendance2, attendance1));

        DashboardAttendanceResponse attendanceResponse = DashboardAttendanceResponse
                .builder()
                .current(AttendanceResponse.builder()
                        .date(TimeResponse.builder().start(attendance2.getStartTime()).build())
                        .location(LocationResponse.builder().type(AttendanceLocationType.OUTSIDE).build())
                        .build())
                .latest(AttendanceResponse.builder()
                        .date(TimeResponse.builder().start(attendance1.getStartTime()).end(attendance1.getEndTime()).build())
                        .location(LocationResponse.builder().type(AttendanceLocationType.INSIDE).build())
                        .build())
                .build();

        CalendarResponse calendarResponse = CalendarResponse.builder()
                .status(CalendarStatus.WORKING)
                .date(startOfDate)
                .build();

        ReportResponse reportResponse = ReportResponse.builder()
                .absent(0)
                .working(0)
                .build();

        IncomingRequestTotalResponse requestTotalResponse = IncomingRequestTotalResponse.builder()
                .incoming(10)
                .build();
        DashboardResponse expected = DashboardResponse.builder()
                .attendance(attendanceResponse)
                .calendar(calendarResponse)
                .report(reportResponse)
                .request(requestTotalResponse)
                .build();

        getDashboardSummaryCommand.execute(user.getUsername())
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).findByDate(startOfDate);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).save(report);
        Mockito.verify(eventRepository, Mockito.times(1)).findByDate(startOfDate);
        Mockito.verify(requestRepository, Mockito.times(1)).countByStatus(RequestStatus.REQUESTED);
        Mockito.verify(attendanceRepository, Mockito.times(1)).findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable);
    }
}
