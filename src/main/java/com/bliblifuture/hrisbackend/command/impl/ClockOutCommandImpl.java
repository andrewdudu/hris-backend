package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.ClockInClockOutResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ClockOutCommandImpl implements ClockOutCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<ClockInClockOutResponse> execute(ClockInClockOutRequest request) {
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findByUsername(username))
                .flatMap(this::getTodayAttendance)
                .map(attendance -> updateAttendance(attendance, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(this::createResponse);
    }

    private Attendance updateAttendance(Attendance attendance, ClockInClockOutRequest request) {
        attendance.setEndLat(request.getLocation().getLat());
        attendance.setEndLon(request.getLocation().getLon());
        attendance.setEndTime(dateUtil.getNewDate());

        return attendance;
    }

    private ClockInClockOutResponse createResponse(Attendance attendance) {
        LocationResponse locationResponse = LocationResponse.builder().lat(attendance.getStartLat()).lon(attendance.getStartLon()).build();
        ClockInClockOutResponse response = ClockInClockOutResponse.builder()
                .locationResponse(locationResponse)
                .build();

        return response;
    }

    @SneakyThrows
    private Mono<Attendance> getTodayAttendance(User user) {
        Date date = dateUtil.getNewDate();
        String dateString = date.getDate() + "/" + date.getMonth() + 1 + "/" + date.getYear() + 1900;

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .parse(dateString + startTime);

        return attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate)
                .map(this::checkValidity);
    }

    private Attendance checkValidity(Attendance attendance) {
        if (attendance.getStartTime() == null){
            throw new NullPointerException("Clock-out not available");
        }
        return attendance;
    }

}
