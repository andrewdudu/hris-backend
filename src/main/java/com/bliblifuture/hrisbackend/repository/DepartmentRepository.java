package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Department;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepository extends ReactiveMongoRepository<Department, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Department> findAll(final Pageable pageable);

    Mono<Department> findByName(String name);

}
