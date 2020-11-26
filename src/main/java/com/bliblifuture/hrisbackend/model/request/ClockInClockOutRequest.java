package com.bliblifuture.hrisbackend.model.request;

import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClockInClockOutRequest extends BaseRequest{

    private String image;

    private LocationRequest location;

}
