package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeaveQuotaFormCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveQuotaFormRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveQuotaFormResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
public class GetLeaveQuotaFormCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetLeaveQuotaFormCommand getLeaveQuotaFormCommand(){
            return new GetLeaveQuotaFormCommandImpl();
        }
    }

    @Autowired
    private GetLeaveQuotaFormCommand getLeaveQuotaFormCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_executeAnnual() throws ParseException, IOException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder()
                .code("ANNUAL_LEAVE")
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 10:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Leave leave = Leave.builder()
                .remaining(8)
                .used(2)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2022-01-01"))
                .type(LeaveType.annual)
                .employeeId(user.getEmployeeId())
                .code("ANNUAL")
                .build();

        Mockito.when(leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                        user.getEmployeeId(), LeaveType.annual, currentDate
                )).thenReturn(Flux.just(leave));

        LeaveQuotaFormResponse expected = LeaveQuotaFormResponse.builder()
                .leaveQuota(8)
                .build();

        getLeaveQuotaFormCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1))
                .getNewDate();
        Mockito.verify(userRepository, Mockito.times(1))
                .findFirstByUsername(user.getUsername());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                        user.getEmployeeId(), LeaveType.annual, currentDate
                );
    }

    @Test
    public void test_executeExtra() throws ParseException, IOException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder()
                .code("EXTRA_LEAVE")
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 10:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Leave leave = Leave.builder()
                .remaining(2)
                .used(0)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2022-01-01"))
                .type(LeaveType.extra)
                .employeeId(user.getEmployeeId())
                .code("EXTRA")
                .build();

        Mockito.when(leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                user.getEmployeeId(), LeaveType.extra, currentDate
        )).thenReturn(Flux.just(leave));

        LeaveQuotaFormResponse expected = LeaveQuotaFormResponse.builder()
                .leaveQuota(2)
                .build();

        getLeaveQuotaFormCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1))
                .getNewDate();
        Mockito.verify(userRepository, Mockito.times(1))
                .findFirstByUsername(user.getUsername());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                        user.getEmployeeId(), LeaveType.extra, currentDate
                );
    }

    @Test
    public void test_executeSubstitute() throws ParseException, IOException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder()
                .code("SUBSTITUTE_LEAVE")
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse("2021-01-10 10:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Leave leave1 = Leave.builder()
                .remaining(1)
                .used(0)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2022-01-01"))
                .type(LeaveType.substitute)
                .employeeId(user.getEmployeeId())
                .code("SUBSTITUTE")
                .build();

        Leave leave2 = Leave.builder()
                .remaining(1)
                .used(0)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2022-01-01"))
                .type(LeaveType.substitute)
                .employeeId(user.getEmployeeId())
                .code("SUBSTITUTE")
                .build();

        Mockito.when(leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                user.getEmployeeId(), LeaveType.substitute, currentDate
        )).thenReturn(Flux.just(leave1, leave2));

        LeaveQuotaFormResponse expected = LeaveQuotaFormResponse.builder()
                .leaveQuota(2)
                .build();

        getLeaveQuotaFormCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(dateUtil, Mockito.times(1))
                .getNewDate();
        Mockito.verify(userRepository, Mockito.times(1))
                .findFirstByUsername(user.getUsername());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                        user.getEmployeeId(), LeaveType.substitute, currentDate
                );
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_executeError() throws ParseException {
        User user = User.builder()
                .username("username")
                .password("encryptedPassword")
                .roles(Arrays.asList(UserRole.ADMIN, UserRole.EMPLOYEE))
                .employeeId("ID-123")
                .build();

        LeaveQuotaFormRequest request = LeaveQuotaFormRequest.builder()
                .code("ERROR_LEAVE")
                .build();
        request.setRequester(user.getUsername());

        getLeaveQuotaFormCommand.execute(request).subscribe();

        Mockito.verify(dateUtil, Mockito.times(0))
                .getNewDate();
        Mockito.verify(userRepository, Mockito.times(0))
                .findFirstByUsername(user.getUsername());
        Mockito.verify(leaveRepository, Mockito.times(0))
                .findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(
                        user.getEmployeeId(), LeaveType.extra, Mockito.any()
                );
    }

}
