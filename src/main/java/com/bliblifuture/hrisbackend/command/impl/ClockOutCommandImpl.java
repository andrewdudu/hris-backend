package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
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
import java.util.concurrent.TimeUnit;

@Service
public class ClockOutCommandImpl implements ClockOutCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<AttendanceResponse> execute(ClockInClockOutRequest request) {
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findFirstByUsername(username))
                .flatMap(user -> getTodayAttendance(user, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(this::createResponse);
    }

    private AttendanceResponse createResponse(Attendance attendance) {
        LocationResponse location = LocationResponse.builder()
                .lat(attendance.getEndLat()).lon(attendance.getEndLon()).build();
        AttendanceResponse response = AttendanceResponse.builder()
                .location(location)
                .build();

        return response;
    }

    @SneakyThrows
    private Mono<Attendance> getTodayAttendance(User user, ClockInClockOutRequest request) {
        Date currentTime = dateUtil.getNewDate();
        String dateString = (currentTime.getYear() + 1900) + "-" + (currentTime.getMonth() + 1) + "-" + currentTime.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        return attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate)
                .doOnSuccess(attendance -> checkValidity(attendance, currentTime))
                .map(attendance -> {
                    attendance.setEndTime(currentTime);
                    attendance.setEndLat(request.getLocation().getLat());
                    attendance.setEndLon(request.getLocation().getLon());
                    return attendance;
                });
    }

    private void checkValidity(Attendance attendance, Date currentTime) {
        if (attendance == null){
            String errorsMessage = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }

        long availableClockoutTime = attendance.getStartTime().getTime() + TimeUnit.HOURS.toMillis(9);
        Date clockoutAvailable = new Date(availableClockoutTime);
        if (attendance.getStartTime() == null || currentTime.before(clockoutAvailable) || attendance.getEndTime() != null){
            String errorsMessage = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

}
