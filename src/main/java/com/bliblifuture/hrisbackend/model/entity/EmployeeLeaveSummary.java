package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "daily_attendance_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EmployeeLeaveSummary extends BaseEntity{

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "year")
    private String year;

    @Field(name = "sick")
    private int sick;

    @Field(name = "child_baptism")
    private int childBaptism;

    @Field(name = "child_circumsion")
    private int childCircumsion;

    @Field(name = "child_birth")
    private int childBirth;

    @Field(name = "hajj")
    private int hajj;

    @Field(name = "maternity")
    private int maternity;

    @Field(name = "main_family_death")
    private int mainFamilyDeath;

    @Field(name = "close_family_death")
    private int closeFamilyDeath;

    @Field(name = "unpaid_leave")
    private int unpaidLeave;

    @Field(name = "annual_leave")
    private int annualLeave;

    @Field(name = "extra_leave")
    private int extraLeave;

    @Field(name = "substitute_leave")
    private int substituteLeave;

}
