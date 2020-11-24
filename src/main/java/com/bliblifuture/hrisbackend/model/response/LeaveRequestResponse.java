package com.bliblifuture.hrisbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse extends BaseResponse{

    private List<String> dates;

    private List<String> files;

    private String notes;

    private String type;
}
