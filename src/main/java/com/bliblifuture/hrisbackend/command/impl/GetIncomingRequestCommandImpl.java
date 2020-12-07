package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    @Override
    public Mono<List<IncomingRequestResponse>> execute(GetIncomingRequest request) {
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> getResponse(user, request.getType())
                        .flatMap(incomingRequest -> requestResponseHelper.createResponse(incomingRequest))
                        .collectList()
                );
    }

    public Flux<Request> getResponse(User user, String type){
        if (user.getRoles().contains(UserRole.MANAGER)){
            return requestRepository.findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus.valueOf(type), user.getUsername())
                    .switchIfEmpty(Flux.empty());
        }
        return requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(type))
                .switchIfEmpty(Flux.empty());
    }

}
