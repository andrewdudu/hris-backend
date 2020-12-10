package com.bliblifuture.hrisbackend.model.request;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetHolidayRequest extends BaseRequest{

    @NotBlank
    private String name;

    private String notes;

    @NotEmpty
    private CalendarStatus status;

    private Date date;

}
