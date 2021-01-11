package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.EmployeeLeaveSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeLeaveSummaryRepository extends ReactiveMongoRepository<EmployeeLeaveSummary, String> {

    @Query("{ id: { $exists: true }}")
    Flux<EmployeeLeaveSummary> findAll(final Pageable pageable);

    Mono<EmployeeLeaveSummary> findFirstByYearAndEmployeeId(String year, String employeeId);

}
