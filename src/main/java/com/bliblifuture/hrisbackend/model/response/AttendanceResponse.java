package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceStatus;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
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

    private TimeResponse date;

    private LocationResponse location;

    private String image;

    private String notes;

    private AttendanceStatus status;
}
