package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.model.response.util.AttendanceLocationType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendance")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AttendanceEntity extends BaseEntity{

    @Field(name = "image")
    private String image;

    @Field(name = "location")
    private AttendanceLocationType location;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "end_time")
    private Date endTime;

    @Field(name = "lat")
    private double lat;

    @Field(name = "lon")
    private double lon;

    @Field(name = "employee_id")
    private String employeeId;
}
