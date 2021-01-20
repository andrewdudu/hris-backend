package com.bliblifuture.hrisbackend.repository;

import com.bliblifuture.hrisbackend.model.entity.EmployeeIndex;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EmployeeElasticsearchRepository extends ReactiveElasticsearchRepository<EmployeeIndex, String> {

    @Query("{\"bool\": {\"must\": {\"wildcard\": {\"name\": \"?0\"} } } }")
    Flux<EmployeeIndex> search(String keyword);

    @Query("{ \"bool\" : " +
                "{ \"must\" : [ " +
                    "{ \"query_string\" : { \"query\" : \"?1\", \"fields\" : [ \"departmentId\" ] } }," +
                    "{ \"query_string\" : { \"query\" : \"*?0*\", \"fields\" : [ \"name\" ] } } ] " +
                "}" +
            "}")
    Flux<EmployeeIndex> search(String keyword, String departmentId);

}
