package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import reactor.core.publisher.Mono;

public abstract class RequestHelper {

    public abstract Mono<Request> processRequest(LeaveRequestData data, User user);

}
