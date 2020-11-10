package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.constant.RequestLeaveStatus;
import com.bliblifuture.hrisbackend.model.entity.RequestLeave;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface RequestLeaveRepository extends ReactiveMongoRepository<RequestLeave, String> {

    @Query("{ id: { $exists: true }}")
    Flux<RequestLeave> findAll(final Pageable pageable);

    Flux<RequestLeave> findByDatesAfterAndStatusAndEmployeeId(Date currentDate, RequestLeaveStatus status, String employeeId);

    Mono<Integer> countByCreatedDateAfterAndStatus(Date currentDate, RequestLeaveStatus status);

}
