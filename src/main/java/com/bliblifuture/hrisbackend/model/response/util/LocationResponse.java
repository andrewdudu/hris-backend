package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.constant.AttendanceLocationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private AttendanceLocationType type;

    private double lat;

    private double lon;

}
