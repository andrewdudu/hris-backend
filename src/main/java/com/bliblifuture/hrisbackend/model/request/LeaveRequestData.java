package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.Pattern;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestData extends BaseRequest{

    private List<@Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "INVALID FORMAT") String> dates;

    private List<@Pattern(regexp = "^[a-z]*;[^;]*$") String> files;

    private String notes;

    private String type;
}
