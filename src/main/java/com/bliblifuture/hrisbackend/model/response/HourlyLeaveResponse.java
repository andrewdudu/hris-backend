package com.bliblifuture.hrisbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyLeaveResponse extends BaseResponse {

    private String startTime;

    private String endTime;

    private String notes;

    private List<String> dates;

}
