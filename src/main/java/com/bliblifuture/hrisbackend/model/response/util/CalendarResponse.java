package com.bliblifuture.hrisbackend.model.response.util;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CalendarResponse {

    private Date date;

    private CalendarStatus status;
}
