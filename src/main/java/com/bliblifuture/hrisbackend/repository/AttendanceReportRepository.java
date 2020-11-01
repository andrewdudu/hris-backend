package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.AttendanceReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AttendanceReportRepository extends ReactiveMongoRepository<AttendanceReportEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<AttendanceReportEntity> findAll(final Pageable pageable);

    Mono<AttendanceReportEntity> findByEmployeeIdAndYear(String id, String year);

}
