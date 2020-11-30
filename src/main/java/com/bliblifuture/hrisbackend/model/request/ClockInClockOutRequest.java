package com.bliblifuture.hrisbackend.model.request;

import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import lombok.*;

import javax.validation.Valid;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClockInClockOutRequest extends BaseRequest{

    private String image;

    @Valid
    private LocationRequest location;

}
