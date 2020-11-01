package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendace")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DepartmentEntity extends BaseEntity {

    @Field(name = "name")
    private String name;

    @Field(name = "code")
    private String code;
}
