package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeRepository extends ReactiveMongoRepository<Employee, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Employee> findAll(final Pageable pageable);

    Mono<Employee> findByEmail(String username);

    Flux<Employee> findByDepId(String depId);

}
