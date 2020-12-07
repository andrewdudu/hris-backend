package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class GetDashboardSummaryCommandImpl implements GetDashboardSummaryCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<DashboardResponse> execute(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::getResponse);
    }

    @SneakyThrows
    private Mono<DashboardResponse> getResponse(User user) {
        Date now = dateUtil.getNewDate();
        String startDate = (now.getYear()+1900) + "-" + (now.getMonth()+1) + "-" + now.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(startDate + startTime);

        CalendarResponse calendarResponse = CalendarResponse.builder().date(startOfDate).build();
        DashboardResponse response = DashboardResponse
                .builder()
                .calendar(calendarResponse)
                .build();

        if (user.getRoles().contains(UserRole.ADMIN)){
            ReportResponse report = new ReportResponse();
            IncomingRequestResponse request = new IncomingRequestResponse();
            response.setReport(report);
            response.setRequest(request);

            return dailyAttendanceReportRepository.findByDate(startOfDate)
                    .switchIfEmpty(
                            Mono.just(DailyAttendanceReport.builder()
                            .date(startOfDate)
                            .working(0)
                            .absent(0)
                            .build())
                    )
                    .doOnSuccess(this::checkNewEntity)
                    .flatMap(res -> {
                        response.getReport().setWorking(res.getWorking());
                        response.getReport().setAbsent(res.getAbsent());
                        return eventRepository.findByDate(startOfDate);
                    })
                    .map(event -> setCalendarResponse(startOfDate, response, event))
                    .flatMap(res -> requestRepository.countByCreatedDateAfterAndStatus(startOfDate, RequestStatus.REQUESTED))
                    .map(totalIncomingRequest -> {
                        response.getRequest().setIncoming(totalIncomingRequest);
                        return response;
                    });
        }
        Pageable pageable = PageRequest.of(0, 2);

        return attendanceRepository.findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable).collectList()
                .map(attendanceList -> {
                    return setAttendanceResponse(attendanceList, response, startOfDate);
                })
                .flatMap(res -> eventRepository.findByDate(startOfDate))
                .switchIfEmpty(Mono.just(Event.builder().build()))
                .map(event -> setCalendarResponse(startOfDate, response, event));
    }

    private void checkNewEntity(DailyAttendanceReport report) {
        if (report.getId() == null){
            Date date = dateUtil.getNewDate();
            report.setCreatedBy("SYSTEM");
            report.setCreatedDate(date);
            report.setUpdatedBy("SYSTEM");
            report.setUpdatedDate(date);
            report.setId("DAR" + report.getDate());

            dailyAttendanceReportRepository.save(report).subscribe();
        }
    }

    private DashboardResponse setAttendanceResponse(List<Attendance> res, DashboardResponse response, Date currentStartDate) {
        AttendanceResponse current = AttendanceResponse.builder().build();
        AttendanceResponse latest = AttendanceResponse.builder().build();

        if (res.get(0).getStartTime().before(currentStartDate)){
            Attendance latestAttendance = res.get(0);
            latest.setDate(
                    AttendanceTimeResponse.builder()
                            .start(latestAttendance.getStartTime())
                            .end(latestAttendance.getEndTime())
                            .build()
            );
            latest.setLocation(
                    LocationResponse.builder()
                            .type(latestAttendance.getLocationType())
                            .build()
            );
        }
        else{
            Attendance latestAttendance = res.get(1);
            latest.setDate(
                    AttendanceTimeResponse.builder()
                            .start(latestAttendance.getStartTime())
                            .end(latestAttendance.getEndTime())
                            .build()
            );
            latest.setLocation(
                    LocationResponse.builder()
                            .type(latestAttendance.getLocationType())
                            .build()
            );

            Attendance currentAttendance = res.get(0);
            current.setDate(
                    AttendanceTimeResponse.builder()
                            .start(currentAttendance.getStartTime())
                            .end(currentAttendance.getEndTime())
                            .build()
            );
            current.setLocation(
                    LocationResponse.builder()
                            .type(currentAttendance.getLocationType())
                            .build()
            );
        }

        response.setAttendance(
                DashboardAttendanceResponse.builder().current(current).latest(latest).build()
        );
        return response;
    }

    private DashboardResponse setCalendarResponse(Date currentDate, DashboardResponse response, Event event) {
        if (event.getId() == null){
            response.getCalendar().setStatus(CalendarStatus.WORKING);
        }
        else{
            response.getCalendar().setStatus(event.getStatus());
        }
        response.getCalendar().setDate(currentDate);
        return response;
    }

}
