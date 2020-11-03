package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.LeaveEntity;
import com.bliblifuture.hrisbackend.model.entity.RequestLeaveEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface RequestLeaveRepository extends ReactiveMongoRepository<RequestLeaveEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<LeaveEntity> findAll(final Pageable pageable);

    Flux<LeaveEntity> findByDateAfterAndStatus(Date currentDate, String status);

    Mono<Integer> countByCreatedDateAfterAndStatus(Date currentDate, String status);

}
