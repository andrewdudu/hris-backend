package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.model.entity.AttendanceEntity;
import com.bliblifuture.hrisbackend.model.entity.UserEntity;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.Location;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ClockOutCommandImpl implements ClockOutCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Mono<AttendanceResponse> execute(AttendanceRequest request) {
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findByUsername(username))
                .flatMap(this::getTodayAttendance)
                .map(attendance -> updateAttendance(attendance, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(this::createResponse);
    }

    private AttendanceEntity updateAttendance(AttendanceEntity attendance, AttendanceRequest request) {
        attendance.setEndLat(request.getLocation().getLat());
        attendance.setEndLon(request.getLocation().getLon());
        attendance.setEndTime(new Date());

        return attendance;
    }

    private AttendanceResponse createResponse(AttendanceEntity attendance) {
        Location location = Location.builder().lat(attendance.getStartLat()).lon(attendance.getStartLon()).build();
        AttendanceResponse response = AttendanceResponse.builder()
                .location(location)
                .build();

        return response;
    }

    @SneakyThrows
    private Mono<AttendanceEntity> getTodayAttendance(UserEntity user) {
        Date now = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(7));
        String theDate = now.getDate() + "/" + now.getMonth() + "/" + now.getYear();

        String startTime = " 00:00:00";
        Date currentStartOfDate = new SimpleDateFormat("dd/MM/yy mm:hh:ss")
                .parse(theDate + startTime);

        return attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), currentStartOfDate)
                .map(this::checkValidity);
    }

    private AttendanceEntity checkValidity(AttendanceEntity attendance) {
        if (attendance.getStartTime() == null){
            throw new NullPointerException("Clock-out not available");
        }
        return attendance;
    }

}
