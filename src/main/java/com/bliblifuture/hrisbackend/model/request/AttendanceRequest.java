package com.bliblifuture.hrisbackend.model.request;

import com.bliblifuture.hrisbackend.model.request.util.LocationRequest;
import lombok.*;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequest extends BaseRequest{

    private String image;

    @NotBlank
    private LocationRequest location;

}
