package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestHourlyLeaveCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.HourlyLeaveRequest;
import com.bliblifuture.hrisbackend.model.response.HourlyLeaveResponse;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class RequestHourlyLeaveCommandImpl implements RequestHourlyLeaveCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @Override
    public Mono<HourlyLeaveResponse> execute(HourlyLeaveRequest request) {
        return employeeRepository.findByEmail(request.getRequester())
                .map(employee -> createRequest(employee, request))
                .flatMap(requestEntity -> requestRepository.save(requestEntity))
                .map(requestEntity -> createResponse(request));
    }

    private HourlyLeaveResponse createResponse(HourlyLeaveRequest request) {
        return HourlyLeaveResponse.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .notes(request.getNotes())
                .build();
    }

    @SneakyThrows
    private Request createRequest(Employee employee, HourlyLeaveRequest request) {
        Date currentTime = dateUtil.getNewDate();

        Request requestEntity = Request.builder()
                .employeeId(employee.getId())
                .manager(employee.getManagerUsername())
                .status(RequestStatus.REQUESTED)
                .notes(request.getNotes())
                .departmentId(employee.getDepId())
                .type(RequestType.HOURLY_LEAVE)
                .build();
        requestEntity.setId("HRL-" + employee.getId() + "-" + currentTime.getTime());
        requestEntity.setCreatedBy(employee.getEmail());
        requestEntity.setCreatedDate(currentTime);

        String thisDate = (currentTime.getYear()+1900) + "-" + (currentTime.getMonth() + 1) + "-" + currentTime.getDate();
        System.out.println(thisDate);
        Date startTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(thisDate + " " + request.getStartTime() + ":00");
        Date endTime = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(thisDate + " " + request.getEndTime() + ":00");

        requestEntity.setStartTime(startTime);
        requestEntity.setEndTime(endTime);

        return requestEntity;
    }

}
