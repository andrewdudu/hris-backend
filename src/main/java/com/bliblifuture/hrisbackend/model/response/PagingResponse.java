package com.bliblifuture.hrisbackend.model.response;

import com.blibli.oss.common.paging.Paging;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagingResponse<T> {

    private List<T> data;

    private Paging paging;

    public PagingResponse(){
    }

    public PagingResponse(List<T> data, Paging paging){
        this.data = data;
        this.paging = paging;
    }

    public PagingResponse<T> setPagingDetail(PagingRequest request, int total){
        int totalPage = (int) Math.ceil(total/(float)request.getSize());
        Paging paging = Paging.builder()
                .page(request.getPage())
                .itemPerPage(request.getSize())
                .totalItem(total)
                .totalPage(totalPage)
                .build();
        this.setPaging(paging);
        return this;
    }
}
