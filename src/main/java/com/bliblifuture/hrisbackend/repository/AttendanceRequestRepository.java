package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.AttendanceRequest;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AttendanceRequestRepository extends ReactiveMongoRepository<AttendanceRequest, String> {

    @Query("{ id: { $exists: true }}")
    Flux<LeaveRequest> findAll(final Pageable pageable);

}
