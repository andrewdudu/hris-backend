package com.bliblifuture.hrisbackend.model.request;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetLeavesDetailRequest extends BaseRequest{

    private String department;

    private int month;
}
