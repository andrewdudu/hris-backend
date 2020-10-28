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
public class Attendance {

    @Field(name = "image")
    private String image;

    @Field(name = "location")
    private String location;

    @Field(name = "start_time")
    private Time startTime;

    @Field(name = "end_time")
    private String endTime;

    @Field(name = "lat")
    private double lat;

    @Field(name = "lon")
    private double lon;

    @Field(name = "assign_by")
    private String assignBy;
}
