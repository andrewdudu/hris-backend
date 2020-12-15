package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Request;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface RequestRepository extends ReactiveMongoRepository<Request, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Request> findAll(final Pageable pageable);

    Mono<Request> findByEmployeeIdAndTypeAndStatus(String employeeId, RequestType type, RequestStatus status);

    Flux<Request> findByDatesAfterAndStatusAndEmployeeId(Date currentDate, RequestStatus status, String employeeId);

    Mono<Request> findByEmployeeIdAndTypeAndDatesContains(String employeeId, RequestType type, Date date);

    Flux<Request> findByStatusOrderByCreatedDateDesc(RequestStatus status);

    Flux<Request> findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus status, String manager);

    Mono<Long> countByStatus(RequestStatus status);

    Mono<Long> countByStatusAndManager(RequestStatus status, String manager);

}
