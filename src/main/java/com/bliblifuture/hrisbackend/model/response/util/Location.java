package com.bliblifuture.hrisbackend.model.response.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private AttendanceLocationType type;

    private double lat;

    private double lon;

}
