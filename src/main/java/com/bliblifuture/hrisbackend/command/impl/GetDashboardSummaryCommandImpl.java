package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.DashboardResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.repository.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    @Override
    public Mono<DashboardResponse> execute(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::getResponse);
    }

    @SneakyThrows
    private Mono<DashboardResponse> getResponse(User user) {

        Date now = new Date();
        String startDate = now.getDate() + "/" + now.getMonth()+1 + "/" + now.getYear()+1900;

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .parse(startDate + startTime);

        CalendarResponse calendarResponse = CalendarResponse.builder().date(startOfDate).build();
        DashboardResponse response = DashboardResponse
                .builder()
                .calendarResponse(calendarResponse)
                .build();

        if (user.getRoles().contains("ADMIN")){
            ReportResponse report = new ReportResponse();
            RequestResponse request = new RequestResponse();
            response.setReportResponse(report);
            response.setRequestResponse(request);

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
                        response.getReportResponse().setWorking(res.getWorking());
                        response.getReportResponse().setAbsent(res.getAbsent());
                        return eventRepository.findByDate(startOfDate);
                    })
                    .map(event -> setCalendarResponse(startOfDate, response, event))
                    .flatMap(res -> requestRepository.countByCreatedDateAfterAndStatus(startOfDate, RequestStatus.PENDING))
                    .map(totalIncomingRequest -> {
                        response.getRequestResponse().setIncoming(totalIncomingRequest);
                        return response;
                    });
        }

        Pageable pageable = PageRequest.of(0, 2);

        return attendanceRepository.findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable).collectList()
                .map(attendanceList -> setAttendanceResponse(attendanceList, response, startOfDate))
                .flatMap(res -> eventRepository.findByDate(startOfDate))
                .map(event -> setCalendarResponse(startOfDate, response, event));
    }

    private void checkNewEntity(DailyAttendanceReport report) {
        if (report.getId() == null){
            Date date = new Date();
            report.setCreatedBy("SYSTEM");
            report.setCreatedDate(date);
            report.setUpdatedBy("SYSTEM");
            report.setUpdatedDate(date);
            report.setId("DAR" + report.getDate());

            dailyAttendanceReportRepository.save(report).subscribe();
        }
    }

    private DashboardResponse setAttendanceResponse(List<Attendance> res, DashboardResponse response, Date currentStartDate) {
        AttendanceTimeResponse date = AttendanceTimeResponse.builder().build();
        LocationResponse locationResponse = LocationResponse.builder().build();

        AttendanceResponse current = AttendanceResponse.builder().attendance(date).locationResponse(locationResponse).build();
        AttendanceResponse latest = AttendanceResponse.builder().attendance(date).locationResponse(locationResponse).build();

        if (res.get(0).getStartTime().before(currentStartDate)){
            Attendance latestAttendance = res.get(0);
            latest.getAttendance().setStart(latestAttendance.getStartTime());
            latest.getAttendance().setEnd(latestAttendance.getEndTime());
            latest.getLocationResponse().setType(latestAttendance.getLocation());

            current.getAttendance().setStart(null);
            current.getAttendance().setEnd(null);
            current.getLocationResponse().setType(null);
        }
        else{
            Attendance latestAttendance = res.get(1);
            latest.getAttendance().setStart(latestAttendance.getStartTime());
            latest.getAttendance().setEnd(latestAttendance.getEndTime());
            latest.getLocationResponse().setType(latestAttendance.getLocation());

            Attendance currentAttendance = res.get(0);
            current.getAttendance().setStart(currentAttendance.getStartTime());
            current.getAttendance().setEnd(currentAttendance.getEndTime());
            current.getLocationResponse().setType(currentAttendance.getLocation());
        }

        response.setAttendance(Arrays.asList(current, latest));
        return response;
    }

    private DashboardResponse setCalendarResponse(Date currentDate, DashboardResponse response, Event event) {
        response.getCalendarResponse().setDate(currentDate);
        response.getCalendarResponse().setStatus(getStatusHoliday(response, event));
        return response;
    }

    private String getStatusHoliday(DashboardResponse res, Event event){
        if (event.getStatus().equals("HOLIDAY")){
            res.getCalendarResponse().setStatus(event.getStatus());
        }
        return "WORKING";
    }

}
