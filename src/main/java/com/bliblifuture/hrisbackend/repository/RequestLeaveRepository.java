package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Leave;
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
    Flux<Leave> findAll(final Pageable pageable);

    Flux<Leave> findByDateAfterAndStatus(Date currentDate, String status);

    Mono<Integer> countByCreatedDateAfterAndStatus(Date currentDate, String status);

}
