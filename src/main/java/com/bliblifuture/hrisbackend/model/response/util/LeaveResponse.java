package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LeaveResponse {

    private LeaveType type;

    private int remaining;

    private int used;

    private Date expiry;

    private List<Date> expiries;
}
