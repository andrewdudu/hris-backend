package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.Leave;
import com.bliblifuture.hrisbackend.model.response.util.Office;
import com.bliblifuture.hrisbackend.model.response.util.Position;
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

    private Position position;

    private Office office;

    private Date joinDate;

    private Leave leave;
}
