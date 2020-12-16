package com.bliblifuture.hrisbackend.model.response;

import lombok.Data;

import java.util.Date;

@Data
public abstract class BaseResponse {

    private String id;

    private Date createdDate;

    private String createdBy;

    private Date LastModifiedDate;

    private String LastModifiedBy;
}
