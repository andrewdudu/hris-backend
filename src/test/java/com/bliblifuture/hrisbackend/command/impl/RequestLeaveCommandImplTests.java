package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.*;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveResponse;
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

        Mockito.when(userRepository.findByUsername(user.getUsername()))
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

        Mockito.when(eventRepository.findByDateAndStatus(date1, CalendarStatus.HOLIDAY))
                .thenReturn(Mono.empty());

        Mockito.when(eventRepository.findByDateAndStatus(date2, CalendarStatus.HOLIDAY))
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

        RequestLeaveResponse expected = RequestLeaveResponse.builder()
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

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(1)).save(req);
        Mockito.verify(eventRepository, Mockito.times(1)).findByDateAndStatus(date1, CalendarStatus.HOLIDAY);
        Mockito.verify(eventRepository, Mockito.times(1)).findByDateAndStatus(date2, CalendarStatus.HOLIDAY);
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(user.getEmployeeId());
    }

}
