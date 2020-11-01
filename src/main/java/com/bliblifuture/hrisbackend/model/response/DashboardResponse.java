package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.Calendar;
import com.bliblifuture.hrisbackend.model.response.util.Report;
import com.bliblifuture.hrisbackend.model.response.util.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Report report;

    private Request request;

    private Calendar calendar;

    private List<AttendanceResponse> attendance;

}
