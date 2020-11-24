package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestLeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface LeaveRequestRepository extends ReactiveMongoRepository<LeaveRequest, String> {

    @Query("{ id: { $exists: true }}")
    Flux<LeaveRequest> findAll(final Pageable pageable);

    Flux<LeaveRequest> findByDatesAfterAndStatusAndEmployeeId(Date currentDate, RequestStatus status, String employeeId);

    Mono<LeaveRequest> findByEmployeeIdAndTypeAndDatesContains(String employeeId, RequestLeaveType type, Date date);

    Mono<Integer> countByCreatedDateAfterAndStatus(Date currentDate, RequestStatus status);

}
