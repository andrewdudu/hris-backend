package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RejectRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.RequestResponse;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RejectRequestCommandImpl implements RejectRequestCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private JavaMailSender emailSender;

    @SneakyThrows
    @Override
    public Mono<RequestResponse> execute(BaseRequest data) {
        return requestRepository.findById(data.getId())
                .doOnSuccess(this::checkValidity)
                .map(request -> approvedRequest(data, request))
                .flatMap(request -> requestRepository.save(request))
                .map(request -> sendEmail(request))
                .flatMap(request -> requestResponseHelper.createResponse(request));
    }

    private Request sendEmail(Request request) {
        String emailDest = request.getCreatedBy();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("blibli");
        mail.setTo(emailDest);
        String type = getType(request);
        mail.setSubject(type.replace("_", " ") + " REJECTED");
        mail.setText("Your " + type.toLowerCase().replace("_", " ") + " request has been rejected");
        emailSender.send(mail);

        return request;
    }

    private String getType(Request request) {
        RequestType type = request.getType();
        if (type.equals(RequestType.SPECIAL_LEAVE)){
            return request.getSpecialLeaveType().toString().toUpperCase();
        }
        return type.toString().toUpperCase();
    }

    private void checkValidity(Request data) {
        if (data == null || data.getStatus().equals(RequestStatus.APPROVED) || data.getStatus().equals(RequestStatus.REJECTED)){
            String msg = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(msg);
        }
    }

    private Request approvedRequest(BaseRequest data, Request request) {
        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedBy(data.getRequester());
        request.setUpdatedBy(data.getRequester());
        request.setUpdatedDate(dateUtil.getNewDate());

        return request;
    }

}
