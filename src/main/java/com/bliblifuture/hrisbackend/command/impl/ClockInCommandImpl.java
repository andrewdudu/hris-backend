package com.bliblifuture.hrisbackend.command.impl;

import com.blibli.oss.command.exception.CommandValidationException;
import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.constant.AttendanceConfig;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.model.entity.AttendanceEntity;
import com.bliblifuture.hrisbackend.model.entity.OfficeEntity;
import com.bliblifuture.hrisbackend.model.entity.UserEntity;
import com.bliblifuture.hrisbackend.model.request.AttendanceRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.response.util.Location;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ClockInCommandImpl implements ClockInCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Override
    public Mono<AttendanceResponse> execute(AttendanceRequest request) {
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findByUsername(username))
                .map(user -> createAttendance(user, request))
                .flatMap(attendance -> clockInProcess(attendance, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .map(this::createResponse);
    }

    private AttendanceResponse createResponse(AttendanceEntity attendance) {
        Location location = Location.builder().lat(attendance.getStartLat()).lon(attendance.getStartLon()).build();
        AttendanceResponse response = AttendanceResponse.builder()
                .image(attendance.getImage())
                .location(location)
                .build();

        return response;
    }

    private Mono<AttendanceEntity> clockInProcess(AttendanceEntity attendance, AttendanceRequest request) {
        return officeRepository.findAll()
                .collectList()
                .map(officeList -> checkLocationAndImage(attendance, request.getImage(), officeList));
    }

    private AttendanceEntity checkLocationAndImage(AttendanceEntity attendance, String imageBase64, List<OfficeEntity> officeList) {
        AttendanceLocationType type = AttendanceLocationType.OUTSIDE;
        for (int i = 0; i < officeList.size(); i++) {
            OfficeEntity office = officeList.get(i);
            double distance = Math.sqrt( Math.pow(attendance.getStartLat() - office.getLat(), 2) + Math.pow(attendance.getStartLat() - office.getLat(), 2) );

            if (distance < AttendanceConfig.RADIUS_ALLOWED){
                type = AttendanceLocationType.INSIDE;
                attendance.setOfficeCode(office.getCode());
                break;
            }

            else if (i == officeList.size()-1){
                if (imageBase64 == null || imageBase64.isEmpty()){
                    throw new CommandValidationException(Collections.singleton("REQUIRED")); //Failed Attendance
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
                    throw new CommandValidationException(Collections.singleton("INVALID_FORMAT"));
                }

                attendance.setImage(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename);
            }
        }
        attendance.setLocation(type);
        return attendance;
    }

    @SneakyThrows
    private AttendanceEntity createAttendance(UserEntity user, AttendanceRequest request) {
        Date dateWithOffset = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis(7));
        String startDate = dateWithOffset.getDate() - 1 + "/" + dateWithOffset.getMonth() + "/" + dateWithOffset.getYear();

        String startTime = " 17:00:00";
        Date currentStartOfDate = new SimpleDateFormat("dd/MM/yy HH:mm:ss")
                .parse(startDate + startTime);

        AttendanceEntity attendance = AttendanceEntity.builder()
                .employeeId(user.getEmployeeId())
                .date(currentStartOfDate)
                .startTime(new Date())
                .endTime(null)
                .startLat(request.getLocation().getLat())
                .startLon(request.getLocation().getLon())
                .build();

        return attendance;
    }

}
