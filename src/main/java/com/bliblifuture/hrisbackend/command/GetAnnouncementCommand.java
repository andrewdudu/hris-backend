package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;

public interface GetAnnouncementCommand extends Command<PagingRequest, PagingResponse<AnnouncementResponse>> {

}
