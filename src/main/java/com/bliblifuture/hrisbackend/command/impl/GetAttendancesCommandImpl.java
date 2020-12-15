package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAttendancesCommand;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.request.AttendanceListRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAttendancesCommandImpl implements GetAttendancesCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Mono<List<AttendanceResponse>> execute(AttendanceListRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .flatMap(user -> attendanceRepository
                        .findByEmployeeIdAndStartTimeBetweenOrderByStartTimeDesc(user.getEmployeeId(), request.getStartDate(), request.getEndDate())
                        .switchIfEmpty(Flux.empty())
                        .collectList())
                .map(this::getResponse);
    }

    private List<AttendanceResponse> getResponse(List<Attendance> attendances) {
        List<AttendanceResponse> responses = new ArrayList<>();
        for (int i = 0; i < attendances.size(); i++) {
            Attendance attendance = attendances.get(i);

            TimeResponse date = TimeResponse.builder()
                    .start(attendance.getStartTime())
                    .end(attendance.getEndTime())
                    .build();

            LocationResponse location = LocationResponse.builder()
                    .lat(attendance.getStartLat())
                    .lon(attendance.getStartLon())
                    .type(attendance.getLocationType())
                    .build();

            responses.add(
                    AttendanceResponse.builder()
                            .date(date)
                            .location(location)
                            .build()
            );
        }
        return responses;
    }

}
