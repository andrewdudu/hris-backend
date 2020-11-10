package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.OrganizationUnitResponse;
import com.bliblifuture.hrisbackend.model.response.util.PositionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private String nik;

    private String name;

    private Department department;

    private PositionResponse positionResponse;

    private OrganizationUnitResponse organizationUnitResponse;

    private OfficeResponse officeResponse;
}
