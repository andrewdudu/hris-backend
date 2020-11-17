package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.RequestStatus;
import com.bliblifuture.hrisbackend.constant.RequestType;
import com.bliblifuture.hrisbackend.constant.SpecialLeaveType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "leave")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Request extends BaseEntity {

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "notes")
    private String notes;

    @Field(name = "type")
    private RequestType type;

    @Field(name = "special_request_type")
    private SpecialLeaveType specialLeaveType;

    @Field(name = "clock_in")
    private String clockIn;

    @Field(name = "clock_out")
    private String clockOut;

    @Field(name = "detail")
    private String detail;

    @Field(name = "dates")
    private List<Date> dates;

    @Field(name = "file")
    private String file;

    @Field(name = "approved_by")
    private String approvedBy;

    @Field(name = "status")
    private RequestStatus status;

}
