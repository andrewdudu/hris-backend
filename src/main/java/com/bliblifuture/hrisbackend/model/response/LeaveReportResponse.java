package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.LeaveQuotaResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeavesDataSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveReportResponse extends BaseResponse{

    private int attendance;

    private LeavesDataSummaryResponse leave;

    private LeaveQuotaResponse quota;
}
