package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "request")
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

    @Field(name = "dates")
    private List<Date> dates;

    @Field(name = "clock_in")
    private Date clockIn;

    @Field(name = "clock_out")
    private Date clockOut;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "end_time")
    private Date endTime;

    @Field(name = "files")
    private List<String> files;

    @Field(name = "manager")
    private String manager;

    @Field(name = "department_id")
    private String departmentId;

    @Field(name = "approved_by")
    private String approvedBy;

    @Field(name = "status")
    private RequestStatus status;

}
