package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendance_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AttendanceReportEntity extends BaseEntity{

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "year")
    private String year;

    @Field(name = "working")
    private int working;

    @Field(name = "absent")
    private int absent;
}
