package com.bliblifuture.hrisbackend.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HourlyLeaveRequest extends BaseRequest{

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    @NotBlank
    private String notes;

}
