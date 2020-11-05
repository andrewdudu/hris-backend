package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.AttendanceTime;
import com.bliblifuture.hrisbackend.model.response.util.LocationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse extends BaseResponse{

    private AttendanceTime attendance;

    private LocationResponse locationResponse;

    private String image;
}
