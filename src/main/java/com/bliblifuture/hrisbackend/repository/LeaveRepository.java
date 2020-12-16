package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface LeaveRepository extends ReactiveMongoRepository<Leave, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Leave> findAll(final Pageable pageable);

    Mono<Leave> findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(String employeeId, LeaveType type, Date date);

    Flux<Leave> findByEmployeeIdAndExpDateAfter(String username, Date currentDate);

    Flux<Leave> findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(String employeeId, LeaveType type, Date date);

    Flux<Leave> findByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateDesc(String employeeId, LeaveType type, Date date);

    Flux<Leave> findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThan(String employeeId, LeaveType type, Date date, int remainingGreaterThan);

    Flux<Leave> findByEmployeeIdAndExpDateAfterAndTypeOrType(String username, Date currentDate, LeaveType type1, LeaveType type2);

}
