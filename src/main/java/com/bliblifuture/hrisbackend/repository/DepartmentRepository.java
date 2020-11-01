package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.DepartmentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DepartmentRepository extends ReactiveMongoRepository<DepartmentEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<DepartmentEntity> findAll(final Pageable pageable);

}
