package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
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
public class Event extends BaseEntity {

    @Field(name = "title")
    private String title;

    @Field(name = "date")
    private Date date;

    @Field(name = "description")
    private String description;

    @Field(name = "status")
    private CalendarStatus status;
}
