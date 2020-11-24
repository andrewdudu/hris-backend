package com.bliblifuture.hrisbackend.model.response.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ExtendLeaveQuotaResponse {

    private int remaining;

    private Date extensionDate;
}
