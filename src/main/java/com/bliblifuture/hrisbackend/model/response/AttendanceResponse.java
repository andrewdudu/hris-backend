package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.AttendanceTimeResponse;
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

    private AttendanceTimeResponse date;

    private LocationResponse location;

    private String image;
}
