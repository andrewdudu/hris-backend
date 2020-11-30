package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetExtendLeaveDataCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.ExtendLeaveQuotaResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
public class GetExtendLeaveDataCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetExtendLeaveDataCommand getExtendLeaveDataCommand(){
            return new GetExtendLeaveDataCommandImpl();
        }
    }

    @Autowired
    private GetExtendLeaveDataCommand getExtendLeaveDataCommand;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username").build();

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-10 10:00:00");
        Date extensionDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2021-3-1 00:00:00");

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Leave annualLeave = Leave.builder()
                .type(LeaveType.annual)
                .employeeId(user.getEmployeeId())
                .expDate(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2021-1-1 00:00:00"))
                .remaining(5)
                .used(7)
                .build();
        Mockito.when(leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(user.getEmployeeId(), LeaveType.annual, currentDate))
                .thenReturn(Flux.just(annualLeave));

        Mockito.when(requestRepository.findByEmployeeIdAndTypeAndDatesContains(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, extensionDate))
                .thenReturn(Mono.empty());

        ExtendLeaveQuotaResponse quota = ExtendLeaveQuotaResponse.builder()
                .remaining(5)
                .extensionDate(extensionDate)
                .build();

        ExtendLeaveResponse expected = ExtendLeaveResponse.builder()
                .status(RequestStatus.AVAILABLE)
                .quota(quota)
                .build();

        getExtendLeaveDataCommand.execute(user.getUsername())
                .subscribe(response -> {
                    Assert.assertEquals(expected.getStatus(), response.getStatus());
                    Assert.assertEquals(expected.getQuota(), response.getQuota());
                });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(user.getUsername());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(1)).findByEmployeeIdAndTypeAndDatesContains(user.getEmployeeId(), RequestType.EXTEND_ANNUAL_LEAVE, extensionDate);
        Mockito.verify(leaveRepository, Mockito.times(1)).findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(user.getEmployeeId(), LeaveType.annual, currentDate);
    }

}
