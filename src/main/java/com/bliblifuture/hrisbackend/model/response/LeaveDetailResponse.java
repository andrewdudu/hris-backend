package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.EmployeeDataResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDetailResponse extends BaseResponse{

    private EmployeeDataResponse employee;

    private String dateString;

    private String typeLabel;

    private List<String> files;

    private TimeResponse date;

    private String approvedBy;

    private String notes;

}
