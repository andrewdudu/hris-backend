package com.bliblifuture.hrisbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLeaveResponse extends BaseResponse{

    private List<String> dates;

    private List<String> files;

    private Date startTime;

    private Date endTime;

    private String notes;

    private String type;
}
