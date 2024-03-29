package com.bliblifuture.hrisbackend.model.entity;

import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
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
public class Leave extends BaseEntity{

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "code")
    private String code;

    @Field(name = "type")
    private LeaveType type;

    @Field(name = "remaining")
    private int remaining;

    @Field(name = "used")
    private int used;

    @Field(name = "expiry_date")
    private Date expDate;
}
