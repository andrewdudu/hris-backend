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

    Flux<Request> findByStatusAndEmployeeId(RequestStatus status, String employeeId);

    Flux<Request> findByEmployeeId(String employeeId);

    Flux<Request> findByStatusAndManagerOrderByCreatedDateDesc(RequestStatus status, String manager);

    Flux<Request> findByDatesContainsAndStatus(Date date, RequestStatus status);

    Mono<Long> countByStatus(RequestStatus status);

    Mono<Long> countByStatusAndManager(RequestStatus status, String manager);

    Flux<Request> findByDatesBetweenAndStatus(Date startDate, Date endDate, RequestStatus status);

    Flux<Request> findByDepartmentIdAndDatesBetweenAndStatus(String depId, Date start, Date end, RequestStatus status);

    Flux<Request> findByDepartmentIdAndStatus(String depId, RequestStatus status);

}
