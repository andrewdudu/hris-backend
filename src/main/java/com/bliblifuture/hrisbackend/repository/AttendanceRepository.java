package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Attendance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface AttendanceRepository extends ReactiveMongoRepository<Attendance, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Attendance> findAll(final Pageable pageable);

    Mono<Long> countByEmployeeIdAndDateAfter(String employeeId, Date date);

    Flux<Attendance> findAllByEmployeeIdOrderByStartTimeDesc(String employeeId, Pageable pageable);

    Flux<Attendance> findByEmployeeIdAndStartTimeBetweenOrderByStartTimeDesc(String employeeId, Date start, Date end);

    Mono<Attendance> findFirstByEmployeeIdAndDate(String employeeId, Date date);

    Flux<Attendance> findByDateAndStartTimeNotNullAndEndTimeIsNull(Date date);

}
