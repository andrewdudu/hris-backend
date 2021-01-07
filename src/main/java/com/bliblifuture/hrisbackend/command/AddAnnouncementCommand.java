package com.bliblifuture.hrisbackend.command;

import com.blibli.oss.command.Command;
import com.bliblifuture.hrisbackend.model.request.AnnouncementRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;

public interface AddAnnouncementCommand extends Command<AnnouncementRequest, AnnouncementResponse> {

}
