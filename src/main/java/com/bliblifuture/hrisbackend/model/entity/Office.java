package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "office")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Office extends BaseEntity{

    @Field(name = "name")
    private String name;

    @Field(name = "code")
    private String code;

    @Field(name = "lat")
    private double lat;

    @Field(name = "lon")
    private double lon;
}
