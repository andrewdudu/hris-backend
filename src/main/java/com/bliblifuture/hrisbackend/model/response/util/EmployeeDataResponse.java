package com.bliblifuture.hrisbackend.model.response.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EmployeeDataResponse {

    private String nik;

    private String name;

    private PositionResponse position;

    private DepartmentResponse department;

    private OrganizationUnitResponse organizationUnit;

    private OfficeResponse office;

}
