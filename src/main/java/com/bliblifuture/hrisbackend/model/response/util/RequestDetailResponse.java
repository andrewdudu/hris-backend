package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.model.response.AttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.ExtendLeaveResponse;
import com.bliblifuture.hrisbackend.model.response.LeaveRequestResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RequestDetailResponse {

    private AttendanceResponse attendance;

    private LeaveRequestResponse leave;

    private ExtendLeaveResponse extend;

}
