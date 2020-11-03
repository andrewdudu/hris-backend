package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.LeaveEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;

@Repository
public interface LeaveRepository extends ReactiveMongoRepository<LeaveEntity, String> {

    @Query("{ id: { $exists: true }}")
    Flux<LeaveEntity> findAll(final Pageable pageable);

    Flux<LeaveEntity> findByEmployeeIdAndExpDateAfter(String username, Date currentDate);

}
