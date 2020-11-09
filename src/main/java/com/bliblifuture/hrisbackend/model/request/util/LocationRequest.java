package com.bliblifuture.hrisbackend.model.request.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationRequest {

    @NotBlank(message = "REQUIRED")
    private double lat;

    @NotBlank(message = "REQUIRED")
    private double lon;

}
