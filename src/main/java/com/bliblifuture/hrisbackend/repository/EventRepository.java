package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.EventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<EventEntity> findAll(final Pageable pageable);

    Flux<EventEntity> findAllByDateAfterOrderByDateDesc(Date date);

}
