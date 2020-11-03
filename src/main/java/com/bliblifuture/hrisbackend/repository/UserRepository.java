package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<UserEntity> findAll(final Pageable pageable);

    Mono<UserEntity> findByUsername(String username);

}
