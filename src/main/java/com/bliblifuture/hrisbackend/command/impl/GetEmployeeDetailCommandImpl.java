package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetEmployeeDetailCommand;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeDetailResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class GetEmployeeDetailCommandImpl implements GetEmployeeDetailCommand {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<EmployeeDetailResponse> execute(String employeeId) {
        EmployeeDetailResponse response = EmployeeDetailResponse.builder().build();
        return employeeRepository.findById(employeeId)
                .doOnSuccess(this::checkNull)
                .flatMap(this::createResponse)
                .flatMap(employeeResponse -> {
                    response.setUser(employeeResponse);

                    Date currentTime = dateUtil.getNewDate();
                    String dateString = (currentTime.getYear() + 1900) + "-" + (currentTime.getMonth() + 1) + "-" + currentTime.getDate();

                    String startTime = " 00:00:00";
                    Date startOfDate;
                    try {
                        startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                                .parse(dateString + startTime);
                    }
                    catch (Exception e){
                        throw new RuntimeException("PARSING_FAILED");
                    }

                    return attendanceRepository.findFirstByEmployeeIdAndDate(employeeResponse.getId(), startOfDate)
                            .switchIfEmpty(Mono.just(Attendance.builder().build()))
                            .map(attendance -> setAttendance(response, attendance));
                });
    }

    private EmployeeDetailResponse setAttendance(EmployeeDetailResponse response, Attendance attendance) {
        response.setAttendance(
                AttendanceResponse.builder()
                        .date(TimeResponse.builder()
                                .start(attendance.getStartTime())
                                .end(attendance.getEndTime())
                                .build())
                        .location(LocationResponse.builder()
                                .type(attendance.getLocationType())
                                .lat(attendance.getStartLat())
                                .lon(attendance.getStartLon())
                                .build())
                        .image(attendance.getImage())
                        .build()
        );
        return response;
    }

    private Mono<EmployeeResponse> createResponse(Employee employee){
        EmployeeResponse employeeResponse = EmployeeResponse.builder()
                .name(employee.getName())
                .id(employee.getId())
                .gender(employee.getGender())
                .build();

        return officeRepository.findById(employee.getOfficeId())
                .flatMap(office -> {
                    employeeResponse.setOffice(OfficeResponse.builder().name(office.getName()).build());
                    return departmentRepository.findById(employee.getDepId());
                })
                .map(department -> {
                   employeeResponse.setDepartment(department.getName());
                   return employeeResponse;
                });
    }

    private void checkNull(Employee employee) {
        if (employee == null){
            String msg = "id=NOT_EXISTS";
            throw new IllegalArgumentException(msg);
        }
    }

}
