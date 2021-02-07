package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.*;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveDetailResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@RunWith(SpringRunner.class)
public class RequestLeaveCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public RequestLeaveCommand requestLeaveCommand(){
            return new RequestLeaveCommandImpl();
        }
    }

    @Autowired
    private RequestLeaveCommand requestLeaveCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void testRequestAnnualLeave_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "ANNUAL-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "ANNUAL_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Leave annualLeave = Leave.builder()
                .remaining(10)
                .code("ANNUAL")
                .employeeId(employee.getId())
                .type(LeaveType.annual)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT)
                        .parse("2021-01-01"))
                .build();
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0)
        ).thenReturn(Flux.just(annualLeave));

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.ANNUAL_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);
        req.setCreatedBy(user.getUsername());
        req.setCreatedDate(new Date(currentDate.getTime()));

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getFiles(), response.getFiles());
                    Assert.assertEquals(expected.getDates(), response.getDates());
                    Assert.assertEquals(expected.getType(), response.getType());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0);
        Mockito.verify(requestRepository, Mockito.times(1)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestAnnualLeaveQuotaNotAvailable_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "ANNUAL-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "ANNUAL_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0)
        ).thenReturn(Flux.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.ANNUAL_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getFiles(), response.getFiles());
                    Assert.assertEquals(expected.getDates(), response.getDates());
                    Assert.assertEquals(expected.getType(), response.getType());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0);
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = ParseException.class)
    public void testRequestAnnualLeaveInvalidDateFormat_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "ANNUAL-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020/05/25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020/05/26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "ANNUAL_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Leave annualLeave = Leave.builder()
                .remaining(10)
                .code("ANNUAL")
                .employeeId(employee.getId())
                .type(LeaveType.annual)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT)
                        .parse("2021-01-01"))
                .build();
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0)
        ).thenReturn(Flux.just(annualLeave));

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.ANNUAL_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.annual, currentDate, 0);
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test
    public void testRequestSubstituteLeave_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "SUBS-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String type = "SUBSTITUTE_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Leave subsLeave = Leave.builder()
                .remaining(1)
                .code("SUBSTITUTE")
                .employeeId(employee.getId())
                .type(LeaveType.substitute)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT)
                        .parse("2021-01-01"))
                .build();
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.substitute, currentDate, 0)
        ).thenReturn(Flux.just(subsLeave));

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1))
                .type(RequestType.SUBSTITUTE_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);
        req.setCreatedBy(user.getUsername());
        req.setCreatedDate(new Date(currentDate.getTime()));

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1))
                .type(RequestType.SUBSTITUTE_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getDates(), response.getDates());
                    Assert.assertEquals(expected.getType(), response.getType());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.substitute, currentDate, 0);
        Mockito.verify(requestRepository, Mockito.times(1)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestSubstituteLeaveQuotaNotAvailable_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "SUBS-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String type = "SUBSTITUTE_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.substitute, currentDate, 0)
        ).thenReturn(Flux.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1))
                .type(RequestType.SUBSTITUTE_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1))
                .type(RequestType.SUBSTITUTE_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        user.getEmployeeId(), LeaveType.substitute, currentDate, 0);
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test
    public void testRequestExtraLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "EXTRA-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String type = "EXTRA_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Leave extraLeave = Leave.builder()
                .remaining(2)
                .code("EXTRA")
                .employeeId(employee.getId())
                .type(LeaveType.extra)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT)
                        .parse("2021-01-01"))
                .build();
        Mockito.when(leaveRepository.
                findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(
                        user.getEmployeeId(), LeaveType.extra, currentDate)
        ).thenReturn(Mono.just(extraLeave));

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1))
                .type(RequestType.EXTRA_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);
        req.setCreatedBy(user.getUsername());
        req.setCreatedDate(new Date(currentDate.getTime()));

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1))
                .type(RequestType.EXTRA_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getDates(), response.getDates());
                    Assert.assertEquals(expected.getType(), response.getType());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), LeaveType.extra, currentDate);
        Mockito.verify(requestRepository, Mockito.times(1)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestExtraLeaveQoutaNotAvailable_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = "EXTRA-" + user.getEmployeeId() + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String type = "EXTRA_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(leaveRepository.
                findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(
                        user.getEmployeeId(), LeaveType.extra, currentDate)
        ).thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1))
                .type(RequestType.EXTRA_LEAVE)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);
        req.setCreatedBy(user.getUsername());
        req.setCreatedDate(new Date(currentDate.getTime()));

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1))
                .type(RequestType.EXTRA_LEAVE.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), LeaveType.extra, currentDate);
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test
    public void testRequestSickWithMedicalLetter_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString() + "-" + user.getEmployeeId() + "-" + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "SICK_WITH_MEDICAL_LETTER";

        String image1 = "image1.webp";
        String image2 = "image2.webp";

        byte[] file1 = FileUtils.readFileToByteArray(new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\" + image1));
        String file1Base64 = "webp;" + Base64.getEncoder().encodeToString(file1);
        byte[] file2 = FileUtils.readFileToByteArray(new File(FileConstant.BASE_STORAGE_PATH + "\\dummy\\" + image2));
        String file2Base64 = "webp;" + Base64.getEncoder().encodeToString(file2);

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .files(Arrays.asList(file1Base64, file2Base64))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        String pathFile1 = FileConstant.REQUEST_IMAGE_BASE_URL + request.getType() + "-" + user.getEmployeeId()
                + "-1-" + currentDate.getTime() + ".webp";
        String pathFile2 = FileConstant.REQUEST_IMAGE_BASE_URL + request.getType() + "-" + user.getEmployeeId()
                + "-2-" + currentDate.getTime() + ".webp";

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .files(Arrays.asList(pathFile1, pathFile2))
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .files(Arrays.asList(pathFile1, pathFile2))
                .type(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getId(), response.getId());
                    Assert.assertEquals(expected.getFiles(), response.getFiles());
                    Assert.assertEquals(expected.getDates(), response.getDates());
                    Assert.assertEquals(expected.getType(), response.getType());
                    Assert.assertEquals(expected.getNotes(), response.getNotes());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(1)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestSickWithMedicalLetterNoFile_execute() throws ParseException, IOException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString() + "-" + user.getEmployeeId() + "-" + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "SICK_WITH_MEDICAL_LETTER";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = RuntimeException.class)
    public void testRequestSickWithMedicalLetterErrorFile_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");
        String id = SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString() + "-" + user.getEmployeeId() + "-" + currentDate.getTime();

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "SICK_WITH_MEDICAL_LETTER";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .files(Arrays.asList("webp;filesError"))
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Employee employee = Employee.builder()
                .name("Employee 1")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .managerUsername("manager")
                .build();

        Mockito.when(employeeRepository.findById(user.getEmployeeId()))
                .thenReturn(Mono.just(employee));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Request req = Request.builder()
                .dates(Arrays.asList(date1, date2))
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .status(RequestStatus.REQUESTED)
                .employeeId(user.getEmployeeId())
                .departmentId(employee.getDepId())
                .manager(employee.getManagerUsername())
                .build();
        req.setId(id);

        Mockito.when(requestRepository.save(req))
                .thenReturn(Mono.just(req));

        RequestLeaveDetailResponse expected = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER.toString())
                .build();
        expected.setId(id);

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(0)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestSickInvalidQuota_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String type = "SICK";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(0))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), Mockito.any(), currentDate);
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestChildbirthInvalidQuota_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String dateString3 = "2020-05-27";
        Date date3 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString3);
        String type = "CHILDBIRTH";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2, dateString3))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date3, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(0))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), Mockito.any(), currentDate);
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date3, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestMarriageInvalidQuota_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String dateString2 = "2020-05-26";
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString2);
        String dateString3 = "2020-05-27";
        Date date3 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString3);
        String dateString4 = "2020-05-28";
        Date date4 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString4);
        String type = "MARRIAGE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1, dateString2, dateString3, dateString4))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date2, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date3, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findFirstByDateAndStatus(date4, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(0))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), Mockito.any(), currentDate);
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(4)).findFirstByDateAndStatus(Mockito.any(Date.class), CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

    @Test(expected = Exception.class)
    public void testRequestInvalidLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-05-27 10:00:00");

        String dateString1 = "2020-05-25";
        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(dateString1);
        String type = "ERROR_LEAVE";

        LeaveRequestData request = LeaveRequestData.builder()
                .dates(Arrays.asList(dateString1))
                .type(type)
                .build();
        request.setRequester(user.getUsername());

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Mockito.when(eventRepository.findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        requestLeaveCommand.execute(request)
                .subscribe();

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(0))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), Mockito.any(), currentDate);
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(eventRepository, Mockito.times(1)).findFirstByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(0)).findById(user.getEmployeeId());
    }

}
