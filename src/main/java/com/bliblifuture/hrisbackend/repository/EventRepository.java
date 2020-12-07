package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface EventRepository extends ReactiveMongoRepository<Event, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Event> findAll(final Pageable pageable);

    Mono<Event> findByTitleAndDate(String title, Date date);

    Flux<Event> findByDateBetweenOrderByDateAsc(Date after, Date before);

    Flux<Event> findAllByDateAfterOrderByDateAsc(Date date, Pageable pageable);

    Mono<Long> countAllByDateAfter(Date date);

    Mono<Event> findByDate(Date date);

}
