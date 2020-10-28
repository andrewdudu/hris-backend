package com.blibli.hrisbackend.model.entity;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class BaseEntity implements Serializable {

    @Id
    @Field(name = "id")
    private String id;

    @Field(value = "created_by")
    private String createdBy;

    @Field(value = "created_date")
    private Date createdDate;

    @Field(value = "updated_by")
    private String updatedBy;

    @Field(value = "updated_date")
    private Date updatedDate;

//    public <T extends BaseEntity, R extends BaseResponse> R createResponse(T obj, R response){
//        BeanUtils.copyProperties(obj, response);
//        response.setId(obj.getId());
//        response.setCreatedBy(obj.getCreatedBy());
//        response.setCreatedDate(obj.getCreatedDate());
//        response.setUpdatedBy(obj.getUpdatedBy());
//        response.setUpdatedDate(obj.getUpdatedDate());
//        return response;
//    }
}
