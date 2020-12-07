package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.DashboardAttendanceResponse;
import com.bliblifuture.hrisbackend.model.response.util.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.model.response.util.ReportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private ReportResponse report;

    private IncomingRequestResponse request;

    private CalendarResponse calendar;

    private DashboardAttendanceResponse attendance;

}
