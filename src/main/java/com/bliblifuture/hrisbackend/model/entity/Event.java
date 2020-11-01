package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "event")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Event {

    @Field(name = "name")
    private String name;

    @Field(name = "date")
    private Date date;

    @Field(name = "note")
    private String note;

    @Field(name = "status")
    private String status;
}
