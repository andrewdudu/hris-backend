package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "employee")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Employee extends BaseEntity{

    @Field(name = "name")
    private String name;

    @Field(name = "email")
    private String email;

    @Field(name = "image")
    private String image;

    @Field(name = "join_date")
    private Date joinDate;

    @Field(name = "position")
    private String position;

    @Field(name = "dep_id")
    private String depId;

    @Field(name = "organization_unit")
    private String organizationUnit;

    @Field(name = "office_id")
    private String officeId;

}
