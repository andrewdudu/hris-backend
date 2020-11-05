package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.EventEntity;
import com.bliblifuture.hrisbackend.model.entity.OfficeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OfficeRepository extends ReactiveMongoRepository<OfficeEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<EventEntity> findAll(final Pageable pageable);

}