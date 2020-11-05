package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendance_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AttendanceReport extends BaseEntity{

    @Field(name = "date")
    private Date date;

    @Field(name = "working")
    private int working;

    @Field(name = "absent")
    private int absent;
}
