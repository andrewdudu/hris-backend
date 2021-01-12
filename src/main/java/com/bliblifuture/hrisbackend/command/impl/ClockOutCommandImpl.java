package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockOutCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceStatus;
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
        Date currentTime = dateUtil.getNewDate();
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findFirstByUsername(username))
                .flatMap(user -> getTodayAttendance(user, request, currentTime))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(attendance -> createResponse(attendance, currentTime));
    }

    private AttendanceResponse createResponse(Attendance attendance, Date currentTime) {
        LocationResponse location = LocationResponse.builder()
                .lat(attendance.getEndLat()).lon(attendance.getEndLon()).build();
        AttendanceResponse response = AttendanceResponse.builder()
                .location(location)
                .build();

        long availableClockoutTime = attendance.getStartTime().getTime() + TimeUnit.HOURS.toMillis(9);
        Date clockoutEligible = new Date(availableClockoutTime);

        if (currentTime.before(clockoutEligible)){
            response.setStatus(AttendanceStatus.WARNING);
        }
        else {
            response.setStatus(AttendanceStatus.FINISH);
        }

        return response;
    }

    @SneakyThrows
    private Mono<Attendance> getTodayAttendance(User user, ClockInClockOutRequest request, Date currentTime) {
        String dateString = (currentTime.getYear() + 1900) + "-" + (currentTime.getMonth() + 1) + "-" + currentTime.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        return attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate)
                .doOnSuccess(this::checkValidity)
                .map(attendance -> {
                    attendance.setEndTime(currentTime);
                    attendance.setEndLat(request.getLocation().getLat());
                    attendance.setEndLon(request.getLocation().getLon());
                    return attendance;
                });
    }

    private void checkValidity(Attendance attendance) {
        if (attendance == null || attendance.getStartTime() == null || attendance.getEndTime() != null){
            String errorsMessage = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

}
