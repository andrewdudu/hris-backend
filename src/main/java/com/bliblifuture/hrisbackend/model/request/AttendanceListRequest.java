package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceListRequest extends BaseRequest{

    private Date startDate;

    private Date endDate;

    private String username;

}
