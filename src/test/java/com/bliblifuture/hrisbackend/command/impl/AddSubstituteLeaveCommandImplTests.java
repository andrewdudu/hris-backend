package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.AddSubstituteLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.SubstituteLeaveRequest;
import com.bliblifuture.hrisbackend.model.response.SubstituteLeaveResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
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
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
public class AddSubstituteLeaveCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public AddSubstituteLeaveCommand addSubstituteLeaveCommand(){
            return new AddSubstituteLeaveCommandImpl();
        }
    }

    @Autowired
    private AddSubstituteLeaveCommand addSubstituteLeaveCommand;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

    @Test
    public void test_execute() throws ParseException {
        User user = User.builder()
                .username("admin")
                .employeeId("ID-123")
                .build();

        String empId = "id123";
        SubstituteLeaveRequest request = SubstituteLeaveRequest.builder()
                .id(empId)
                .total(2)
                .build();
        request.setRequester(user.getUsername());

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-30 10:00:00");
        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(userRepository.findFirstByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        Date expDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(90));

        String id = "UUID";
        Mockito.when(uuidUtil.getNewID()).thenReturn(id);

        Leave leave = Leave.builder()
                .code("SUBS")
                .employeeId(request.getId())
                .type(LeaveType.substitute)
                .expDate(expDate)
                .remaining(1)
                .used(0)
                .build();
        leave.setId(id);
        leave.setCreatedBy(user.getUsername());
        leave.setCreatedDate(currentDate);

        Mockito.when(leaveRepository.save(leave))
                .thenReturn(Mono.just(leave));

        Mockito.when(leaveRepository
                .countByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThan(
                        empId, LeaveType.substitute, currentDate, 0))
                .thenReturn(Mono.just(3L));

        SubstituteLeaveResponse expected = SubstituteLeaveResponse.builder()
                .total(3)
                .id(empId)
                .build();

        addSubstituteLeaveCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(userRepository, Mockito.times(1)).findFirstByUsername(user.getUsername());
        Mockito.verify(leaveRepository, Mockito.times(request.getTotal())).save(leave);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(2)).getNewID();
    }
}
