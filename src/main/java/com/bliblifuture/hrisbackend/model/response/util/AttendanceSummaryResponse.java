package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.model.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse extends BaseResponse {

    private int attendance;

    private int absent;
}
