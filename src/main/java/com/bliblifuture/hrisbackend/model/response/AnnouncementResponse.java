package com.bliblifuture.hrisbackend.model.response;

import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse extends BaseResponse{

    private String title;

    private Date date;

    private String description;

    private CalendarStatus status;
}
