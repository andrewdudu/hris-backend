package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDashboardSummaryCommand;
import com.bliblifuture.hrisbackend.constant.RequestLeaveStatus;
import com.bliblifuture.hrisbackend.model.entity.AttendanceEntity;
import com.bliblifuture.hrisbackend.model.entity.EventEntity;
import com.bliblifuture.hrisbackend.model.entity.UserEntity;
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
    private Mono<DashboardResponse> getResponse(UserEntity user) {

        Date now = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(7));
        String theDate = now.getDate() + "/" + now.getMonth() + "/" + now.getYear();

        String startTime = " 00:00:00";
        Date currentStartOfDate = new SimpleDateFormat("dd/MM/yy mm:hh:ss")
                .parse(theDate + startTime);

        //        String endTime = " 23:59:59";
//        Date currentEndDate = new SimpleDateFormat("dd/MM/yy mm:hh:ss")
//                .parse(theDate + endTime);

        Calendar calendar = Calendar.builder().date(currentStartOfDate).build();
        DashboardResponse response = DashboardResponse
                .builder()
                .calendar(calendar)
                .build();

        if (user.getRoles().contains("ADMIN")){
            Report report = new Report();
            Request request = new Request();
            response.setReport(report);
            response.setRequest(request);

            return attendanceReportRepository.findByDate(currentStartOfDate)
                    .flatMap(res -> {
                        response.getReport().setWorking(res.getWorking());
                        response.getReport().setAbsent(res.getAbsent());
                        return eventRepository.findByDate(currentStartOfDate);
                    })
                    .map(event -> setCalendarResponse(currentStartOfDate, response, event))
                    .flatMap(res -> requestLeaveRepository.countByCreatedDateAfterAndStatus(currentStartOfDate, RequestLeaveStatus.PENDING))
                    .map(totalIncomingRequest -> {
                        response.getRequest().setIncoming(totalIncomingRequest);
                        return response;
                    });
        }

        Pageable pageable = PageRequest.of(0, 2);

        return attendanceRepository.findAllByEmployeeIdOrderByStartTimeDesc(user.getEmployeeId(),pageable).collectList()
                .map(attendanceList -> setAttendanceResponse(attendanceList, response, currentStartOfDate))
                .flatMap(res -> eventRepository.findByDate(currentStartOfDate))
                .map(event -> setCalendarResponse(currentStartOfDate, response, event));
    }

    private DashboardResponse setAttendanceResponse(List<AttendanceEntity> res, DashboardResponse response, Date currentStartDate) {
        AttendanceTime date = AttendanceTime.builder().build();
        Location location = Location.builder().build();

        AttendanceResponse current = AttendanceResponse.builder().attendance(date).location(location).build();
        AttendanceResponse latest = AttendanceResponse.builder().attendance(date).location(location).build();

        if (res.get(0).getStartTime().before(currentStartDate)){
            AttendanceEntity latestAttendance = res.get(0);
            latest.getAttendance().setStart(latestAttendance.getStartTime());
            latest.getAttendance().setEnd(latestAttendance.getEndTime());
            latest.getLocation().setType(latestAttendance.getLocation());

            current.getAttendance().setStart(null);
            current.getAttendance().setEnd(null);
            current.getLocation().setType(null);
        }
        else{
            AttendanceEntity latestAttendance = res.get(1);
            latest.getAttendance().setStart(latestAttendance.getStartTime());
            latest.getAttendance().setEnd(latestAttendance.getEndTime());
            latest.getLocation().setType(latestAttendance.getLocation());

            AttendanceEntity currentAttendance = res.get(0);
            current.getAttendance().setStart(currentAttendance.getStartTime());
            current.getAttendance().setEnd(currentAttendance.getEndTime());
            current.getLocation().setType(currentAttendance.getLocation());
        }

        response.setAttendance(Arrays.asList(current, latest));
        return response;
    }

    private DashboardResponse setCalendarResponse(Date currentDate, DashboardResponse response, EventEntity event) {
        response.getCalendar().setDate(currentDate);
        response.getCalendar().setStatus(getStatusHoliday(response, event));
        return response;
    }

    private String getStatusHoliday(DashboardResponse res, EventEntity event){
        if (event.getStatus().equals("HOLIDAY")){
            res.getCalendar().setStatus(event.getStatus());
        }
        return "WORKING";
    }

}
