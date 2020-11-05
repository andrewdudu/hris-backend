package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.Leave;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Date;

@Repository
public interface LeaveRepository extends ReactiveMongoRepository<Leave, String> {

    @Query("{ id: { $exists: true }}")
    Flux<Leave> findAll(final Pageable pageable);

    Flux<Leave> findByEmployeeIdAndExpDateAfter(String username, Date currentDate);

}
