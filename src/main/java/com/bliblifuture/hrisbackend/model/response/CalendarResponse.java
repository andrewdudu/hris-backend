package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.model.response.util.EventDetailResponse;
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
public class CalendarResponse {

    private Date date;

    private CalendarStatus status;

    private List<EventDetailResponse> events;

}
