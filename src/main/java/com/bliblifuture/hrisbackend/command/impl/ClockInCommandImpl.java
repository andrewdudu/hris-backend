package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.constant.AttendanceConfig;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.Office;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ClockInCommandImpl implements ClockInCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<AttendanceResponse> execute(ClockInClockOutRequest request) {
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findByUsername(username))
                .map(user -> createAttendance(user, request))
                .flatMap(attendance -> clockInProcess(attendance, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(this::createResponse);
    }

    private AttendanceResponse createResponse(Attendance attendance) {
        LocationResponse location = LocationResponse.builder()
                .lat(attendance.getStartLat())
                .lon(attendance.getStartLon())
                .type(attendance.getLocationType())
                .build();
        AttendanceResponse response = AttendanceResponse.builder()
                .image(attendance.getImage())
                .location(location)
                .build();

        return response;
    }

    private Mono<Attendance> clockInProcess(Attendance attendance, ClockInClockOutRequest request) {
        return officeRepository.findAll()
                .collectList()
                .map(officeList -> checkLocationAndImage(attendance, request.getImage(), officeList));
    }

    private Attendance checkLocationAndImage(Attendance attendance, String imageBase64, List<Office> officeList) {
        AttendanceLocationType type = AttendanceLocationType.OUTSIDE;
        for (int i = 0; i < officeList.size(); i++) {
            Office office = officeList.get(i);
            double distance = Math.sqrt( Math.pow(attendance.getStartLat() - office.getLat(), 2) + Math.pow(attendance.getStartLat() - office.getLat(), 2) );

            if (distance < AttendanceConfig.RADIUS_ALLOWED){
                type = AttendanceLocationType.INSIDE;
                attendance.setOfficeCode(office.getCode());
            }
            else if (i == officeList.size()-1){
                if (imageBase64 == null || imageBase64.isEmpty()){
                    throw new SecurityException("NO_IMAGE"); //Failed Attendance
                }

                String filename = "EMP" + attendance.getEmployeeId() + "_" + attendance.getStartTime() + ".webp";
                String uploadPath = FileConstant.IMAGE_ATTENDANCE_PATH + filename;
                Path path = Paths.get(uploadPath);

                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    imageByte = decoder.decodeBuffer(imageBase64);
                    Files.write(path, imageByte);
                } catch (IOException e) {
                    throw new IllegalArgumentException("INVALID_REQUEST");
                }

                attendance.setImage(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename);
            }
        }
        attendance.setLocationType(type);
        return attendance;
    }

    @SneakyThrows
    private Attendance createAttendance(User user, ClockInClockOutRequest request) {
        Date date = dateUtil.getNewDate();
        String dateString = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        Attendance attendance = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(date)
                .endTime(null)
                .startLat(request.getLocation().getLat())
                .startLon(request.getLocation().getLon())
                .build();
        attendance.setId(UUID.randomUUID().toString());

        return attendance;
    }

}
