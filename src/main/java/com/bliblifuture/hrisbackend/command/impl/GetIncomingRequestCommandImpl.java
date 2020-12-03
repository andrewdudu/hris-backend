package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GetIncomingRequestCommandImpl implements GetIncomingRequestCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @SneakyThrows
    @Override
    public Mono<List<RequestResponse>> execute(String type) {
        return requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(type))
                .switchIfEmpty(Flux.empty())
                .flatMap(request -> requestResponseHelper.createResponse(request))
                .collectList();
    }

}
