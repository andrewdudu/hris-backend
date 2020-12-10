package com.bliblifuture.hrisbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "employee", shards = 3)
public class EmployeeIndex{

    @Id
    private String id;

    private String name;

    private String departmentId;

}
