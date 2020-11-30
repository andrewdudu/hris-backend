package com.bliblifuture.hrisbackend.model.request.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationRequest {

    private double lat;

    private double lon;

}
