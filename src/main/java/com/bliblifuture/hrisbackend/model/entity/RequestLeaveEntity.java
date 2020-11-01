package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "leave")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RequestLeaveEntity extends BaseEntity {

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "note")
    private String note;

    @Field(name = "type")
    private String type;

    @Field(name = "detail")
    private String detail;

    @Field(name = "date")
    private Date date;

    @Field(name = "file")
    private String file;

    @Field(name = "requested_date")
    private String requestedDate;

    @Field(name = "approved_by")
    private String approvedBy;

    @Field(name = "status")
    private String status;

}
