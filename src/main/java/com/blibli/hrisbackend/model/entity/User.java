package com.blibli.hrisbackend.model.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field(name = "email")
    private String email;

    @Field(name = "nik")
    private String nik;

    @Field(name = "roles")
    private List<String> roles;

    @Field(name = "join_date")
    private Boolean joinDate;

    @Field(name = "position")
    private Boolean position;

    @Field(name = "dep_id")
    private String depId;

    @Field(name = "organization_unit")
    private Boolean organizationUnit;

    @Field(name = "office")
    private Boolean office;

//    @Field(name = "leaves")
//    private Boolean leaves;
//
//    @Field(name = "attendances")
//    private String attendances;

}
