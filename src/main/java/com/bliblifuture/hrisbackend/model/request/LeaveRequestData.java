package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestData extends BaseRequest{

    private List<@NotBlank String> dates;

    private List<String> files;

    private String notes;

    @NotBlank
    private String type;
}
