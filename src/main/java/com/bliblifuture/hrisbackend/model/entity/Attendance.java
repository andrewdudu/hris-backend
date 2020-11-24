package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
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
public class Attendance extends BaseEntity{

    @Field(name = "image")
    private String image;

    @Field(name = "location")
    private AttendanceLocationType location;

    @Field(name = "office_code")
    private String officeCode;

    @Field(name = "date")
    private Date date;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "end_time")
    private Date endTime;

    @Field(name = "start_lat")
    private double startLat;

    @Field(name = "start_lon")
    private double startLon;

    @Field(name = "end_lat")
    private double endLat;

    @Field(name = "end_lon")
    private double endLon;

    @Field(name = "employee_id")
    private String employeeId;
}
