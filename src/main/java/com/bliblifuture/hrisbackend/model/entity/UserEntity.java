package com.bliblifuture.hrisbackend.model.entity;

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
public class UserEntity extends BaseEntity{

    @Field(name = "employee_id")
    private String employeeId;

    @Field(name = "username")
    private String username;

    @Field(name = "password")
    private String password;

    @Field(name = "roles")
    private List<String> roles;

}
