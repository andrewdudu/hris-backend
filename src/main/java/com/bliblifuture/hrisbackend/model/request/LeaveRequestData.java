package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestData extends BaseRequest{

    private List<String> dates;

    private List<String> files;

    private String notes;

    private String type;
}
