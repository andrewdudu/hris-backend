package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesQuotaCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetLeavesQuotaCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetLeavesQuotaCommand getLeavesQuotaCommand(){
            return new GetLeavesQuotaCommandImpl();
        }
    }

    @Autowired
    private GetLeavesQuotaCommand getLeavesQuotaCommand;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        String empId = "EMP-123";
        Date currentDate = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date newYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-1-1");
        Leave annual = Leave.builder()
                .type(LeaveType.annual)
                .code("ANNUAL")
                .employeeId(empId)
                .expDate(newYear)
                .used(2)
                .remaining(10)
                .build();
        Leave extra = Leave.builder()
                .type(LeaveType.extra)
                .code("EXTRA")
                .employeeId(empId)
                .expDate(newYear)
                .used(2)
                .remaining(1)
                .build();
        Leave substitute = Leave.builder()
                .type(LeaveType.substitute)
                .code("SUBSTITUTE")
                .employeeId(empId)
                .expDate(newYear)
                .used(0)
                .remaining(1)
                .build();

        Date startOfTheYear = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-1-1");
        Mockito.when(leaveRepository.findByEmployeeIdAndExpDateAfter(empId, startOfTheYear))
                .thenReturn(Flux.just(annual, extra, substitute));

        LeaveResponse annualLeave = LeaveResponse.builder()
                .type(LeaveType.annual)
                .used(2)
                .remaining(10)
                .expiry(newYear)
                .build();
        LeaveResponse extraLeave = LeaveResponse.builder()
                .type(LeaveType.extra)
                .used(2)
                .remaining(1)
                .expiry(newYear)
                .build();
        LeaveResponse substituteLeave = LeaveResponse.builder()
                .type(LeaveType.substitute)
                .used(0)
                .remaining(1)
                .expiries(Arrays.asList(newYear))
                .build();
        List<LeaveResponse> expected = Arrays.asList(annualLeave, extraLeave, substituteLeave);

        getLeavesQuotaCommand.execute(empId)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndExpDateAfter(empId, startOfTheYear);

    }
}
