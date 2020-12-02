package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequestData extends BaseRequest{

    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "INVALID_FORMAT")
    private String date;

    @Pattern(regexp = "^[0-9]{2}:[0-9]{2}$", message = "INVALID_FORMAT")
    private String ClockIn;

    @Pattern(regexp = "^[0-9]{2}:[0-9]{2}$", message = "INVALID_FORMAT")
    private String ClockOut;

    private String notes;

}
