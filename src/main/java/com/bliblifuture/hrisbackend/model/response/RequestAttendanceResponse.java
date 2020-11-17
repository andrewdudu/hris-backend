package com.bliblifuture.hrisbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAttendanceResponse extends BaseResponse{

    private String date;

    private String ClockIn;

    private String ClockOut;

    private String notes;
}
