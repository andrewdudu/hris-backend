package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RejectRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RejectRequestCommandImpl implements RejectRequestCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @Autowired
    private DateUtil dateUtil;

    @SneakyThrows
    @Override
    public Mono<RequestResponse> execute(BaseRequest data) {
        return requestRepository.findById(data.getId())
                .doOnSuccess(this::checkValidity)
                .map(request -> approvedRequest(data, request))
                .flatMap(request -> requestRepository.save(request))
                .flatMap(request -> requestResponseHelper.createResponse(request));
    }

    private void checkValidity(Request data) {
        if (data == null || data.getStatus().equals(RequestStatus.APPROVED) || data.getStatus().equals(RequestStatus.REJECTED)){
            String msg = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(msg);
        }
    }

    private Request approvedRequest(BaseRequest data, Request request) {
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(data.getRequester());
        request.setUpdatedBy(data.getRequester());
        request.setUpdatedDate(dateUtil.getNewDate());

        return request;
    }

}
