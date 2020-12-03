package com.bliblifuture.hrisbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is empty")
    @Email(message = "INVALID_FORMAT")
    private String username;

    @NotBlank(message = "Password is empty")
    private String password;

}
