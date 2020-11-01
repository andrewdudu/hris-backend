package com.bliblifuture.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User extends BaseEntity{

    @Field(name = "username")
    private String username;

    @Field(name = "password")
    private String password;

    @Field(name = "name")
    private String name;

    @Field(name = "nik")
    private String nik;

    @Field(name = "image")
    private String image;

    @Field(name = "roles")
    private List<String> roles;

    @Field(name = "join_date")
    private Date joinDate;

    @Field(name = "position")
    private String position;

    @Field(name = "dep_id")
    private String depId;

    @Field(name = "organization_unit")
    private String organizationUnit;

    @Field(name = "office")
    private String Office;

    @Field(name = "leaves")
    private int leaves;

    @Field(name = "attendances")
    private String attendances;

}
