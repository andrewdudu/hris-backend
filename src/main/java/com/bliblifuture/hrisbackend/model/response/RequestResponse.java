package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse extends BaseResponse{

    private UserResponse user;

    private RequestStatus status;

    private RequestType type;

    private RequestDetailResponse detail;

    private Date date;

    private String approvedby;
}
