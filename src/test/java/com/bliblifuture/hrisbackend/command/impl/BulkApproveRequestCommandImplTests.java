package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.BulkApproveRequestCommand;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.request.BulkApproveRequest;
import com.bliblifuture.hrisbackend.model.response.BulkApproveResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
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
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
public class BulkApproveRequestCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public BulkApproveRequestCommand bulkApproveRequestCommand(){
            return new BulkApproveRequestCommandImpl();
        }
    }

    @Autowired
    private BulkApproveRequestCommand bulkApproveRequestCommand;

    @MockBean
    private ApproveRequestCommand approveRequestCommand;

    @Test
    public void test_execute() throws ParseException, IOException {
        List<String> ids = Arrays.asList("id1", "id2");
        BulkApproveRequest request = BulkApproveRequest.builder()
                .ids(ids)
                .build();

        RequestResponse response1 = RequestResponse.builder().build();
        response1.setId(ids.get(0));

        RequestResponse response2 = RequestResponse.builder().build();
        response2.setId(ids.get(1));

        Mockito.when(approveRequestCommand.execute(new BaseRequest(ids.get(0), request.getRequester())))
                .thenReturn(Mono.just(response1));

        Mockito.when(approveRequestCommand.execute(new BaseRequest(ids.get(1), request.getRequester())))
                .thenReturn(Mono.just(response2));

        BulkApproveResponse expected = BulkApproveResponse.builder().ids(ids).build();

        bulkApproveRequestCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(approveRequestCommand, Mockito.times(1))
                .execute(new BaseRequest(ids.get(0), request.getRequester()));
        Mockito.verify(approveRequestCommand, Mockito.times(1))
                .execute(new BaseRequest(ids.get(1), request.getRequester()));
    }

}
