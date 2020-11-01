package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.AttendanceTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse extends BaseResponse{

    private AttendanceTime attendanceTime;

    private String location;
}
