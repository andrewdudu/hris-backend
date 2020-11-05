package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.AttendanceReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface AttendanceReportRepository extends ReactiveMongoRepository<AttendanceReport, String> {

    @Query("{ id: { $exists: true }}")
    Flux<AttendanceReport> findAll(final Pageable pageable);

    Mono<AttendanceReport> findAllByDateBeforeAndDateAfter(Date startDay, Date endDay);

    Mono<AttendanceReport> findByDate(Date date);

}
