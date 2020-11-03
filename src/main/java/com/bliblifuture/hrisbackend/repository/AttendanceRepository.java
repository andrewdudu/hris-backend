package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.AttendanceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface AttendanceRepository extends ReactiveMongoRepository<AttendanceEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<AttendanceEntity> findAll(final Pageable pageable);

    Mono<Integer> countByStartTimeAfterAndStartTimeBefore(Date start, Date end);

    Flux<AttendanceEntity> findAllByEmployeeIdOrderByStartTimeDesc(String employeeId, Pageable pageable);

}
