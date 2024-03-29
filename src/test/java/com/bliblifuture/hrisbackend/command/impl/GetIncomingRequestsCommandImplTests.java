package com.bliblifuture.hrisbackend.command.impl;

import com.blibli.oss.common.paging.Paging;
import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.*;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetIncomingRequestsCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetIncomingRequestCommand getIncomingRequestCommand(){
            return new GetIncomingRequestCommandImpl();
        }
    }

    @Autowired
    private GetIncomingRequestCommand getIncomingRequestCommand;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private RequestResponseHelper requestResponseHelper;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testByDepartmentAdmin_execute() throws ParseException {
        User admin = User.builder()
                .employeeId("id123")
                .username("username1")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .build();

        String type = "REQUESTED";
        String depCode = "DEP-1";

        GetIncomingRequest request = GetIncomingRequest.builder()
                .type(type)
                .department(depCode)
                .page(0)
                .size(10)
                .build();
        request.setRequester(admin.getUsername());

        Mockito.when(userRepository.findFirstByUsername(request.getRequester()))
                .thenReturn(Mono.just(admin));

        Department department = Department.builder()
                .name("InfoTech")
                .code(depCode)
                .build();

        Mockito.when(departmentRepository.findFirstByCode(request.getDepartment()))
                .thenReturn(Mono.just(department));

        User user1 = User.builder().employeeId("id123").username("username1").build();
        User user2 = User.builder().employeeId("id456").username("username2").build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-21");

        Date date3 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        String notes1 = "notes1";
        String notes2 = "notes2";
        Request request1 = Request.builder()
                .employeeId(user1.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .notes(notes1)
                .build();
        request1.setCreatedDate(date3);

        Request request2 = Request.builder()
                .employeeId(user2.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date2))
                .status(RequestStatus.REQUESTED)
                .notes(notes2)
                .build();
        request2.setCreatedDate(date3);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Mockito.when(requestRepository.findByDepartmentIdAndStatusOrderByCreatedDateDesc(department.getId(), RequestStatus.valueOf(type), pageable))
                .thenReturn(Flux.just(request1, request2));

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(notes1)
                .build();
        RequestDetailResponse detail1 = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();
        RequestResponse data1 = RequestResponse.builder()
                .user(UserResponse.builder().username(user1.getUsername()).employeeId(user1.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.ATTENDANCE)
                .date(date3)
                .detail(detail1)
                .build();

        RequestLeaveDetailResponse leave = RequestLeaveDetailResponse.builder()
                .dates(Collections.singletonList("2020-10-21"))
                .notes(notes2)
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        RequestDetailResponse detail2 = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        RequestResponse data2 = RequestResponse.builder()
                .user(UserResponse.builder().username(user2.getUsername()).employeeId(user2.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.LEAVE)
                .detail(detail2)
                .date(date3)
                .build();

        List<RequestResponse> data = Arrays.asList(data1, data2);

        Mockito.when(requestResponseHelper.createResponse(request1)).thenReturn(Mono.just(data1));
        Mockito.when(requestResponseHelper.createResponse(request2)).thenReturn(Mono.just(data2));

        Mockito.when(requestRepository.countByDepartmentIdAndStatus(department.getId(), RequestStatus.valueOf(type)))
                .thenReturn(Mono.just(2L));

        Paging paging = Paging.builder()
                .totalPage(1)
                .totalItem(2)
                .page(0)
                .itemPerPage(10)
                .build();

        PagingResponse<RequestResponse> expected = new PagingResponse<>();
        expected.setData(data);
        expected.setPaging(paging);

        getIncomingRequestCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(response.getPaging(), expected.getPaging());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request1);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request2);
        Mockito.verify(departmentRepository, Mockito.times(1)).findFirstByCode(request.getDepartment());
        Mockito.verify(requestRepository, Mockito.times(1))
                .findByDepartmentIdAndStatusOrderByCreatedDateDesc(department.getId(), RequestStatus.valueOf(type), pageable);
        Mockito.verify(requestRepository, Mockito.times(1)).countByDepartmentIdAndStatus(department.getId(), RequestStatus.valueOf(type));
    }

    @Test
    public void testAllRequestsAdmin_execute() throws ParseException {
        User admin = User.builder()
                .employeeId("id123")
                .username("username1")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.ADMIN))
                .build();

        String type = "REQUESTED";

        GetIncomingRequest request = GetIncomingRequest.builder()
                .type(type)
                .page(0)
                .size(10)
                .build();
        request.setRequester(admin.getUsername());

        Mockito.when(userRepository.findFirstByUsername(request.getRequester()))
                .thenReturn(Mono.just(admin));

        User user1 = User.builder().employeeId("id123").username("username1").build();
        User user2 = User.builder().employeeId("id456").username("username2").build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-21");

        Date date3 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        String notes1 = "notes1";
        String notes2 = "notes2";
        Request request1 = Request.builder()
                .employeeId(user1.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .notes(notes1)
                .build();
        request1.setCreatedDate(date3);

        Request request2 = Request.builder()
                .employeeId(user2.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date2))
                .status(RequestStatus.REQUESTED)
                .notes(notes2)
                .build();
        request2.setCreatedDate(date3);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Mockito.when(requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.REQUESTED, pageable))
                .thenReturn(Flux.just(request1, request2));

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(notes1)
                .build();
        RequestDetailResponse detail1 = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();
        RequestResponse data1 = RequestResponse.builder()
                .user(UserResponse.builder().username(user1.getUsername()).employeeId(user1.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.ATTENDANCE)
                .date(date3)
                .detail(detail1)
                .build();

        RequestLeaveDetailResponse leave = RequestLeaveDetailResponse.builder()
                .dates(Collections.singletonList("2020-10-21"))
                .notes(notes2)
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        RequestDetailResponse detail2 = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        RequestResponse data2 = RequestResponse.builder()
                .user(UserResponse.builder().username(user2.getUsername()).employeeId(user2.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.LEAVE)
                .detail(detail2)
                .date(date3)
                .build();

        List<RequestResponse> data = Arrays.asList(data1, data2);

        Mockito.when(requestResponseHelper.createResponse(request1)).thenReturn(Mono.just(data1));
        Mockito.when(requestResponseHelper.createResponse(request2)).thenReturn(Mono.just(data2));

        Mockito.when(requestRepository.countByStatus(RequestStatus.REQUESTED))
                .thenReturn(Mono.just(2L));

        Paging paging = Paging.builder()
                .totalPage(1)
                .totalItem(2)
                .page(0)
                .itemPerPage(10)
                .build();

        PagingResponse<RequestResponse> expected = new PagingResponse<>();
        expected.setData(data);
        expected.setPaging(paging);

        getIncomingRequestCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(response.getPaging(), expected.getPaging());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request1);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request2);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findByStatusOrderByCreatedDateDesc(RequestStatus.REQUESTED, pageable);
        Mockito.verify(requestRepository, Mockito.times(1)).countByStatus(RequestStatus.REQUESTED);
    }

    @Test
    public void testManager_execute() throws ParseException {
        User manager = User.builder()
                .employeeId("id123")
                .username("username1")
                .roles(Arrays.asList(UserRole.EMPLOYEE, UserRole.MANAGER))
                .build();

        String type = "REQUESTED";

        GetIncomingRequest request = GetIncomingRequest.builder()
                .type(type)
                .page(0)
                .size(10)
                .build();
        request.setRequester(manager.getUsername());

        Mockito.when(userRepository.findFirstByUsername(request.getRequester()))
                .thenReturn(Mono.just(manager));

        User user1 = User.builder().employeeId("id123").username("username1").build();
        User user2 = User.builder().employeeId("id456").username("username2").build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-21");

        Date date3 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        String notes1 = "notes1";
        String notes2 = "notes2";
        Request request1 = Request.builder()
                .employeeId(user1.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .manager(manager.getUsername())
                .notes(notes1)
                .build();
        request1.setCreatedDate(date3);

        Request request2 = Request.builder()
                .employeeId(user2.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .dates(Collections.singletonList(date2))
                .status(RequestStatus.REQUESTED)
                .manager(manager.getUsername())
                .notes(notes2)
                .build();
        request2.setCreatedDate(date3);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Mockito.when(requestRepository.findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus.REQUESTED, manager.getUsername(), pageable))
                .thenReturn(Flux.just(request1, request2));

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendance = AttendanceResponse.builder()
                .date(date)
                .notes(notes1)
                .build();
        RequestDetailResponse detail1 = RequestDetailResponse.builder()
                .attendance(attendance)
                .build();
        RequestResponse data1 = RequestResponse.builder()
                .user(UserResponse.builder().username(user1.getUsername()).employeeId(user1.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.ATTENDANCE)
                .date(date3)
                .detail(detail1)
                .build();

        RequestLeaveDetailResponse leave = RequestLeaveDetailResponse.builder()
                .dates(Collections.singletonList("2020-10-21"))
                .notes(notes2)
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();
        RequestDetailResponse detail2 = RequestDetailResponse.builder()
                .leave(leave)
                .build();
        RequestResponse data2 = RequestResponse.builder()
                .user(UserResponse.builder().username(user2.getUsername()).employeeId(user2.getEmployeeId()).build())
                .status(RequestStatus.REQUESTED)
                .type(RequestType.LEAVE)
                .detail(detail2)
                .date(date3)
                .build();

        List<RequestResponse> data = Arrays.asList(data1, data2);

        Mockito.when(requestResponseHelper.createResponse(request1)).thenReturn(Mono.just(data1));
        Mockito.when(requestResponseHelper.createResponse(request2)).thenReturn(Mono.just(data2));

        Mockito.when(requestRepository.countByStatusAndManager(RequestStatus.REQUESTED, manager.getUsername()))
                .thenReturn(Mono.just(2L));

        Paging paging = Paging.builder()
                .totalPage(1)
                .totalItem(2)
                .page(0)
                .itemPerPage(10)
                .build();

        PagingResponse<RequestResponse> expected = new PagingResponse<>();
        expected.setData(data);
        expected.setPaging(paging);

        getIncomingRequestCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(response.getPaging(), expected.getPaging());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request1);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(request2);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus.REQUESTED, manager.getUsername(), pageable);
        Mockito.verify(requestRepository, Mockito.times(1))
                .countByStatusAndManager(RequestStatus.REQUESTED, manager.getUsername());
    }

}
