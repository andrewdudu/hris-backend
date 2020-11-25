package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.model.response.util.CalendarResponse;
import com.bliblifuture.hrisbackend.model.response.util.ReportResponse;
import com.bliblifuture.hrisbackend.model.response.util.IncomingRequestResponse;
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

    private ReportResponse report;

    private IncomingRequestResponse request;

    private CalendarResponse calendar;

    private List<AttendanceResponse> attendance;

}
