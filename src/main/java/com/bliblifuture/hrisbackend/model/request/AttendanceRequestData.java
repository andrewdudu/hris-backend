package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequestData extends BaseRequest{

    @NotBlank
    private String date;

    @NotBlank
    private String ClockIn;

    @NotBlank
    private String ClockOut;

    private String notes;

}
