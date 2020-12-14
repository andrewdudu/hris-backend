package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCalendarRequest extends BaseRequest{

    @Min(value = 1, message = "INVALID_REQUEST")
    @Max(value = 12, message = "INVALID_REQUEST")
    private int month;

    private int year;

}
