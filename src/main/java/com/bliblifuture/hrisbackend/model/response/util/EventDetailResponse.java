package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EventDetailResponse {

    private String name;

    private String notes;

    private CalendarStatus status;

}
