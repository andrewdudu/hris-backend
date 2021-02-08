package com.bliblifuture.hrisbackend.model.response.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LeavesDataResponse {

    private int sick;

    private int marriage;

    private int childBaptism;

    private int childCircumsion;

    private int childBirth;

    private int hajj;

    private int maternity;

    private int mainFamilyDeath;

    private int closeFamilyDeath;

    private int unpaidLeave;

    private int annualLeave;

    private int extraLeave;

    private int substituteLeave;

    private int hourlyLeave;

    private int annualLeaveExtension;

    private int requestAttendance;
}
