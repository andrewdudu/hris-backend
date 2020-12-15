package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.DailyAttendanceReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface DailyAttendanceReportRepository extends ReactiveMongoRepository<DailyAttendanceReport, String> {

    @Query("{ id: { $exists: true }}")
    Flux<DailyAttendanceReport> findAll(final Pageable pageable);

    Mono<DailyAttendanceReport> findByDate(Date date);

}
