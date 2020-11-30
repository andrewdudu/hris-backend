package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DashboardAttendanceResponse {

    private AttendanceResponse current;

    private AttendanceResponse latest;
}
