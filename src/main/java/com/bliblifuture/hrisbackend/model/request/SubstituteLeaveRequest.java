package com.bliblifuture.hrisbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubstituteLeaveRequest extends BaseRequest{

    private String id;

    private int total;

}
