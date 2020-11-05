package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.RequestLeaveStatus;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
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
import java.util.concurrent.TimeUnit;

@Service
public class GetDashboardSummaryCommandImpl implements GetDashboardSummaryCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceReportRepository attendanceReportRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RequestLeaveRepository requestLeaveRepository;

    @Override
    public Mono<DashboardResponse> execute(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::getResponse);
    }

    @SneakyThrows
    private Mono<DashboardResponse> getResponse(User user) {

        Date now = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(7));
        String startDate = now.getDate() - 1 + "/" + now.getMonth() + "/" + now.getYear();

        String startTime = " 17:00:00";
        Date currentStartOfDate = new SimpleDateFormat("dd/MM/yy HH:mm:ss")
                .parse(startDate + startTime);

        //        String endTime = " 16:59:59";
//        String endDate = now.getDate() + "/" + now.getMonth() + "/" + now.getYear();
//        Date currentEndDate = new SimpleDateFormat("dd/MM/yy mm:hh:ss")
//                .parse(startDate + endTime);

        CalendarResponse calendarResponse = CalendarResponse.builder().date(currentStartOfDate).build();
        DashboardResponse response = DashboardResponse
                .builder()
                .calendarResponse(calendarResponse)
                .build();

        if (user.getRoles().contains("ADMIN")){
            ReportResponse reportResponse = new ReportResponse();
            RequestResponse requestResponse = new RequestResponse();
            response.setReportResponse(reportResponse);
            response.setRequestResponse(requestResponse);

            return attendanceReportRepository.findByDate(currentStartOfDate)
                    .flatMap(res -> {
                        response.getReportResponse().setWorking(res.getWorking());
                        response.getReportResponse().setAbsent(res.getAbsent());
                        return eventRepository.findByDate(currentStartOfDate);
                    })
                    .map(event -> setCalendarResponse(currentStartOfDate, response, event))
                    .flatMap(res -> requestLeaveRepository.countByCreatedDateAfterAndStatus(currentStartOfDate, RequestLeaveStatus.PENDING))
                    .map(totalIncomingRequest -> {
                        response.getRequestResponse().setIncoming(totalIncomingRequest);
                        return response;
                    });
        }

        Pageable pageable = PageRequest.of(0, 2);

        return attendanceRepository.findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable).collectList()
                .map(attendanceList -> setAttendanceResponse(attendanceList, response, currentStartOfDate))
                .flatMap(res -> eventRepository.findByDate(currentStartOfDate))
                .map(event -> setCalendarResponse(currentStartOfDate, response, event));
    }

    private DashboardResponse setAttendanceResponse(List<Attendance> res, DashboardResponse response, Date currentStartDate) {
        AttendanceTime date = AttendanceTime.builder().build();
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
