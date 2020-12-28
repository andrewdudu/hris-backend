package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetIncomingRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.UserRole;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.GetIncomingRequest;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Mono<PagingResponse<IncomingRequestResponse>> execute(GetIncomingRequest request) {
        if (request.getType() == null || request.getType().isEmpty()){
            String msg = "type=INVALID_REQUEST";
            throw new IllegalArgumentException(msg);
        }

        return userRepository.findByUsername(request.getRequester())
                .flatMap(user -> getRequestsData(user, request));
    }

    public Mono<PagingResponse<IncomingRequestResponse>> getRequestsData(User user, GetIncomingRequest request){
        PagingResponse<IncomingRequestResponse> response = new PagingResponse<>();

        String status = request.getType();
        PagingRequest pagingRequest = PagingRequest.builder()
                .page(request.getPage()).size(request.getSize()).build();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        if (user.getRoles().contains(UserRole.MANAGER)){
            return requestRepository.findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus.valueOf(status), user.getUsername(), pageable)
                    .switchIfEmpty(Flux.empty())
                    .collectList()
                    .flatMap(this::getResponses)
                    .flatMap(data -> {
                        response.setData(data);
                        return requestRepository.countByStatusAndManager(RequestStatus.valueOf(status), user.getUsername());
                    })
                    .map(total -> response.setPagingDetail(pagingRequest, Math.toIntExact(total)));
        }

        if (request.getDepartment() == null || request.getDepartment().isEmpty()){
            return requestRepository.findByStatusOrderByCreatedDateDesc(RequestStatus.valueOf(status), pageable)
                    .switchIfEmpty(Flux.empty())
                    .collectList()
                    .flatMap(this::getResponses)
                    .flatMap(data -> {
                        response.setData(data);
                        return requestRepository.countByStatus(RequestStatus.valueOf(status));
                    })
                    .map(total -> response.setPagingDetail(pagingRequest, Math.toIntExact(total)));
        }

        return departmentRepository.findByCode(request.getDepartment())
                .flatMap(department -> requestRepository
                        .findByDepartmentIdAndStatusOrderByCreatedDateDesc(department.getId(), RequestStatus.valueOf(status), pageable)
                        .collectList()
                        .flatMap(this::getResponses)
                        .flatMap(data -> {
                            response.setData(data);
                            return requestRepository.countByDepartmentIdAndStatus(department.getId(), RequestStatus.valueOf(status));
                        })
                        .map(total -> response.setPagingDetail(pagingRequest, Math.toIntExact(total)))
                );
    }

    public Mono<List<IncomingRequestResponse>> getResponses(List<Request> requests){
        return Flux.fromIterable(requests)
                .flatMap(incomingRequest -> requestResponseHelper.createResponse(incomingRequest))
                .collectList();
    }

}
