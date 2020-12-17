package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
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
    private DepartmentRepository departmentRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    @Override
    public Mono<List<IncomingRequestResponse>> execute(GetIncomingRequest request) {
        if (request.getType() == null || request.getType().isEmpty()){
            String msg = "type=INVALID_REQUEST";
            throw new IllegalArgumentException(msg);
        }
        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> getRequestsData(user, request)
                        .flatMap(requests -> Flux.fromIterable(requests)
                                .flatMap(incomingRequest -> requestResponseHelper.createResponse(incomingRequest))
                                .collectList()
                        ));
    }

    public Mono<List<Request>> getRequestsData(User user, GetIncomingRequest request){
        String status = request.getType();
        if (user.getRoles().contains(UserRole.MANAGER)){
            return requestRepository.findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus.valueOf(status), user.getUsername())
                    .switchIfEmpty(Flux.empty())
                    .collectList();
        }

        if (request.getDepartment() == null || request.getDepartment().isEmpty()){
            return requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(status))
                    .switchIfEmpty(Flux.empty())
                    .collectList();
        }

        return departmentRepository.findByCode(request.getDepartment())
                .flatMap(department -> requestRepository
                        .findByDepartmentIdAndStatusOrderByCreatedDateDesc(department.getId(), RequestStatus.valueOf(status))
                        .collectList()
                );
    }

}
