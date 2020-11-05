package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.PositionResponse;
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
public class UserResponse extends BaseResponse{

    private String username;

    private String name;

    private List<String> roles;

    private String department;

    private PositionResponse positionResponse;

    private OfficeResponse officeResponse;

    private Date joinDate;

    private LeaveResponse leaveResponse;
}
