package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestHourlyLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.HourlyLeaveRequest;
import com.bliblifuture.hrisbackend.model.response.HourlyLeaveResponse;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
public class RequestHourlyLeaveCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public RequestHourlyLeaveCommand requestHourlyLeaveCommand(){
            return new RequestHourlyLeaveCommandImpl();
        }
    }

    @Autowired
    private RequestHourlyLeaveCommand requestHourlyLeaveCommand;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    public void test_execute() throws ParseException, IOException {
        HourlyLeaveRequest request = HourlyLeaveRequest.builder()
                .startTime("09:00")
                .endTime("11:00")
                .notes("Leave")
                .build();

        Employee employee = Employee.builder()
                .name("Employee 1")
                .managerUsername("manager")
                .depId("DEP-1")
                .email("emp@mail.com")
                .build();
        employee.setId("id123");

        Mockito.when(employeeRepository.findFirstByEmail(request.getRequester()))
                .thenReturn(Mono.just(employee));

        Date currentTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 09:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentTime);

        Request entity = Request.builder()
                .employeeId(employee.getId())
                .manager(employee.getManagerUsername())
                .status(RequestStatus.REQUESTED)
                .notes(request.getNotes())
                .departmentId(employee.getDepId())
                .type(RequestType.HOURLY_LEAVE)
                .build();
        entity.setId("HRL-" + employee.getId() + "-" + currentTime.getTime());
        entity.setCreatedBy(employee.getEmail());
        entity.setCreatedDate(currentTime);
        entity.setDates(Arrays.asList(currentTime));

        Date startTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 09:00:00");
        Date endTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 11:00:00");

        entity.setStartTime(startTime);
        entity.setEndTime(endTime);

        Mockito.when(requestRepository.save(entity)).thenReturn(Mono.just(entity));

        HourlyLeaveResponse expected = HourlyLeaveResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .notes(request.getNotes())
                .build();

        requestHourlyLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeRepository, Mockito.times(1))
                .findFirstByEmail(request.getRequester());
        Mockito.verify(requestRepository, Mockito.times(1))
                .save(entity);
    }

}
