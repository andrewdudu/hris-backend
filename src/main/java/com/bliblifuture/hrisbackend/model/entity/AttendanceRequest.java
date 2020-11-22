package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "attendance_request")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AttendanceRequest extends BaseEntity {

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "date")
    private Date date;

    @Field(name = "clock_in")
    private String clockIn;

    @Field(name = "clock_out")
    private String clockOut;

    @Field(name = "notes")
    private String notes;

    @Field(name = "approved_by")
    private String approvedBy;

    @Field(name = "status")
    private RequestStatus status;

}
