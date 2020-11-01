package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.response.util.Office;
import com.bliblifuture.hrisbackend.model.response.util.OrganizationUnit;
import com.bliblifuture.hrisbackend.model.response.util.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse{

    private String nik;

    private String name;

    private Department department;

    private Position position;

    private OrganizationUnit organizationUnit;

    private Office office;
}
