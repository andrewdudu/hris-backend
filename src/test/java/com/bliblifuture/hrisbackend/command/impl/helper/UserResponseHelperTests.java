package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.constant.enumerator.Gender;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.PositionResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
public class UserResponseHelperTests {

    @TestConfiguration
    static class command{
        @Bean
        public UserResponseHelper userResponseHelper(){
            return new UserResponseHelper();
        }
    }

    @Autowired
    private UserResponseHelper userResponseHelper;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private OfficeRepository officeRepository;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .employeeId("id123")
                .username("username")
                .roles(Arrays.asList(UserRole.EMPLOYEE))
                .build();

        Employee employee = Employee.builder()
                .depId("Dep-123")
                .gender(Gender.MALE)
                .name("Employee 1")
                .managerUsername("manager")
                .officeId("OFFICE-1")
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-1-1"))
                .email("email@mail.com")
                .image("")
                .level("3")
                .organizationUnit("01")
                .position("Staff")
                .build();
        employee.setId(user.getEmployeeId());

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Department department = Department.builder()
                .code("DEP-123")
                .name("InfoTech")
                .build();

        Mockito.when(departmentRepository.findById(employee.getDepId()))
                .thenReturn(Mono.just(department));

        Office office = Office.builder()
                .code("OFFICE-1")
                .name("MAIN-OFFICE")
                .build();

        Mockito.when(officeRepository.findById(employee.getOfficeId()))
                .thenReturn(Mono.just(office));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 09:00:00");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Leave annual = Leave.builder()
                .used(10)
                .remaining(2)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-1-1"))
                .type(LeaveType.annual)
                .employeeId(employee.getId())
                .code("ANNUAL")
                .build();

        Mockito.when(leaveRepository
                .findByEmployeeIdAndExpDateAfterAndRemainingGreaterThan(employee.getId(), currentDate, 0)
        ).thenReturn(Flux.just(annual));

        UserResponse expected = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .leave(LeaveResponse.builder()
                        .remaining(annual.getRemaining())
                        .build())
                .joinDate(employee.getJoinDate())
                .office(OfficeResponse.builder().name(office.getName()).build())
                .department(department.getName())
                .name(employee.getName())
                .position(PositionResponse.builder().name(employee.getPosition()).build())
                .build();
        expected.setId(user.getEmployeeId());

        userResponseHelper.getUserResponse(user)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
        Mockito.verify(departmentRepository, Mockito.times(1)).findById(employee.getDepId());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(officeRepository, Mockito.times(1)).findById(employee.getOfficeId());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndExpDateAfterAndRemainingGreaterThan(employee.getId(), currentDate, 0);

    }

}
