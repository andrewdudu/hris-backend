package com.bliblifuture.hrisbackend.model.response.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReportResponse {

    private int working;

    private int absent;
}
