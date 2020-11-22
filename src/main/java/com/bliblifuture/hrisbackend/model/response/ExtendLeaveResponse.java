package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.response.util.ExtendLeaveQuotaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendLeaveResponse extends BaseResponse{

    private RequestStatus status;

    private ExtendLeaveQuotaResponse quota;

    private String notes;
}
