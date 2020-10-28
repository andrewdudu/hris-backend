package com.blibli.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Time;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendace")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Department {

    @Field(name = "name")
    private String name;

    @Field(name = "code")
    private String code;
}
