package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveQuotaFormRequest extends BaseRequest{

    private String code;
}
