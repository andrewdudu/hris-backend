package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.BulkApproveRequestCommand;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.request.BulkApproveRequest;
import com.bliblifuture.hrisbackend.model.response.BulkApproveResponse;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class BulkApproveRequestCommandImpl implements BulkApproveRequestCommand {

    @Autowired
    private ApproveRequestCommand approveRequestCommand;

    @Override
    public Mono<BulkApproveResponse> execute(BulkApproveRequest request) {
        return Flux.fromIterable(request.getIds())
                .flatMap(id -> approveRequestCommand.execute(new BaseRequest(id, request.getRequester())))
                .collectList()
                .map(this::createResponse);
    }

    private BulkApproveResponse createResponse(List<RequestResponse> requestResponses) {
        List<String> ids = new ArrayList<>();

        for (RequestResponse response : requestResponses) {
            ids.add(response.getId());
        }
        return BulkApproveResponse.builder().ids(ids).build();
    }
}
