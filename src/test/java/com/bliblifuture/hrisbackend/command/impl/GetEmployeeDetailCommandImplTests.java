package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetEmployeeDetailCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.Gender;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeDetailResponse;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class GetEmployeeDetailCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetEmployeeDetailCommand getEmployeeDetailCommand(){
            return new GetEmployeeDetailCommandImpl();
        }
    }

    @Autowired
    private GetEmployeeDetailCommand getEmployeeDetailCommand;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private OfficeRepository officeRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        String employeeId = "EMP-123";
        String depId = "DEP-1";

        User user = User.builder().username("username").employeeId(employeeId).build();

        Employee employee = Employee.builder()
                .name("Employee 1")
                .email(user.getUsername())
                .depId(depId)
                .officeId("OFFICE-1")
                .gender(Gender.FEMALE)
                .build();
        employee.setId(employeeId);

        Mockito.when(employeeRepository.findById(employeeId))
                .thenReturn(Mono.just(employee));

        Office office = Office.builder()
                .name("MAIN OFFICE")
                .code("OFFICE-1")
                .build();

        Mockito.when(officeRepository.findById(employee.getOfficeId()))
                .thenReturn(Mono.just(office));

        Department department = Department.builder()
                .code("DEP-1")
                .name("InfoTech")
                .build();

        Mockito.when(departmentRepository.findById(employee.getDepId()))
                .thenReturn(Mono.just(department));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 11:00:00");
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-20");

        Attendance attendance = Attendance.builder()
                .employeeId(user.getEmployeeId())
                .date(startOfDate)
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 08:00:00"))
                .startLat(1.1)
                .startLon(1.2)
                .locationType(AttendanceLocationType.INSIDE)
                .build();
        attendance.setId("att-123");

        Mockito.when(attendanceRepository.findFirstByEmployeeIdAndDate(employee.getId(), startOfDate))
                .thenReturn(Mono.just(attendance));

        EmployeeDetailResponse expected = EmployeeDetailResponse.builder()
                .attendance(AttendanceResponse.builder()
                        .date(TimeResponse.builder().start(attendance.getStartTime()).build())
                        .location(LocationResponse.builder()
                                .type(AttendanceLocationType.INSIDE)
                                .lon(attendance.getStartLon())
                                .lat(attendance.getStartLat())
                                .build())
                        .build())
                .user(EmployeeResponse.builder()
                        .office(OfficeResponse.builder().name(office.getName()).build())
                        .gender(Gender.FEMALE)
                        .name(employee.getName())
                        .department(department.getName())
                        .id(user.getEmployeeId())
                        .build())
                .build();

        getEmployeeDetailCommand.execute(user.getEmployeeId())
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(employeeId);
        Mockito.verify(officeRepository, Mockito.times(1)).findById(employee.getOfficeId());
        Mockito.verify(departmentRepository, Mockito.times(1)).findById(employee.getDepId());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(attendanceRepository, Mockito.times(1)).findFirstByEmployeeIdAndDate(employee.getId(), startOfDate);
    }

}
