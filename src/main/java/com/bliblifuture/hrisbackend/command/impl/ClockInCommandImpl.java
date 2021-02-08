package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ClockInCommand;
import com.bliblifuture.hrisbackend.constant.AttendanceConfig;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import com.bliblifuture.hrisbackend.model.entity.Office;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.ClockInClockOutRequest;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.DailyAttendanceReportRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
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

@Service
public class ClockInCommandImpl implements ClockInCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    @SneakyThrows
    public Mono<AttendanceResponse> execute(ClockInClockOutRequest request) {
        Date currentDate = dateUtil.getNewDate();
        String dateString = (currentDate.getYear() + 1900) + "-" + (currentDate.getMonth() + 1) + "-" + currentDate.getDate();

        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);
        return Mono.fromCallable(request::getRequester)
                .flatMap(username -> userRepository.findFirstByUsername(username))
                .flatMap(user -> createAttendance(user, request, currentDate, startOfDate))
                .flatMap(attendance -> clockInProcess(attendance, request))
                .flatMap(attendance -> attendanceRepository.save(attendance))
                .flatMap(attendance -> dailyAttendanceReportRepository.findFirstByDate(startOfDate)
                        .switchIfEmpty(
                                Mono.just(DailyAttendanceReport.builder()
                                        .date(startOfDate)
                                        .working(0)
                                        .absent(0)
                                        .build()))
                        .map(report -> createOrUpdateAttendanceReport(report, currentDate))
                        .flatMap(report -> dailyAttendanceReportRepository.save(report))
                        .map(dailyAttendanceReport -> attendance))
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

    @SneakyThrows
    private Attendance checkLocationAndImage(Attendance attendance, String base64, List<Office> officeList) {
        AttendanceLocationType type = AttendanceLocationType.OUTSIDE;
        for (int i = 0; i < officeList.size(); i++) {
            Office office = officeList.get(i);
            double distance = calc_distance(office.getLat(), office.getLon(), attendance.getStartLat(), attendance.getStartLon());

            if (distance < AttendanceConfig.RADIUS_ALLOWED_KM){
                type = AttendanceLocationType.INSIDE;
                attendance.setOfficeCode(office.getCode());
            }
            else if (i == officeList.size()-1){
                if (base64 == null || base64.isEmpty()){
                    String errorsMessage = "image=EMPTY_FILE";
                    throw new IllegalArgumentException(errorsMessage);
                }

                String filename = attendance.getEmployeeId() + "_" + attendance.getStartTime().getTime() + ".webp";
                String uploadPath = FileConstant.IMAGE_ATTENDANCE_PATH + filename;
                Path path = Paths.get(uploadPath);

                byte[] imageByte;
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    String[] base64Parts = base64.split(";");
                    imageByte = decoder.decodeBuffer(base64Parts[1]);
                    Files.write(path, imageByte);
                } catch (IOException e) {
                    String errorsMessage = "image=INVALID_FORMAT";
                    throw new IllegalArgumentException(errorsMessage);
                }

                attendance.setImage(FileConstant.IMAGE_ATTENDANCE_BASE_URL + filename);
            }
        }
        attendance.setLocationType(type);
        return attendance;
    }

    private double calc_distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0710;
        double rLat1 = lat1 * (Math.PI/180);
        double rLat2 = lat1 * (Math.PI/180);
        double diffLat = rLat2-rLat1;
        double diffLon = (lon2-lon1) * (Math.PI/180);

        return 2 * R * Math.asin(
                Math.sqrt(Math.sin(diffLat/2) * Math.sin(diffLat/2) + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(diffLon/2) * Math.sin(diffLon/2))
        );
    }

    @SneakyThrows
    private Mono<Attendance> createAttendance(User user, ClockInClockOutRequest request, Date currentTime, Date startOfDate) {
        Attendance attendance = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(currentTime)
                .endTime(null)
                .startLat(request.getLocation().getLat())
                .startLon(request.getLocation().getLon())
                .build();
        attendance.setId(uuidUtil.getNewID());
        attendance.setCreatedDate(currentTime);
        attendance.setCreatedBy(user.getUsername());

        return attendanceRepository.findFirstByEmployeeIdAndDate(user.getEmployeeId(), startOfDate)
                .doOnSuccess(this::checkIfExists)
                .thenReturn(attendance);
    }

    private void checkIfExists(Attendance attendance) {
        if (attendance != null){
            String errorsMessage = "message=ALREADY_CLOCK_IN";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private DailyAttendanceReport createOrUpdateAttendanceReport(DailyAttendanceReport report, Date date) {
        if (report.getId() == null || report.getId().isEmpty()){
            report.setCreatedBy("SYSTEM");
            report.setCreatedDate(date);
            report.setId("DA" + report.getDate().getTime());
        }
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(date);
        report.setWorking(report.getWorking() + 1);

        return report;
    }

}
