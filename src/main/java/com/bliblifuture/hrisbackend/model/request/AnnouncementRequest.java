package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementRequest extends BaseRequest{

    @NotBlank
    private String title;

    private String notes;

}
