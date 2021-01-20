package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.RequestLeaveCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.AnnualLeaveRequestHelper;
import com.bliblifuture.hrisbackend.command.impl.helper.ExtraLeaveRequestHelper;
import com.bliblifuture.hrisbackend.command.impl.helper.SpecialLeaveRequestHelper;
import com.bliblifuture.hrisbackend.command.impl.helper.SubstituteLeaveRequestHelper;
import com.bliblifuture.hrisbackend.constant.LeaveTypeConstant;
import com.bliblifuture.hrisbackend.constant.enumerator.CalendarStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Event;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.request.LeaveRequestData;
import com.bliblifuture.hrisbackend.model.response.RequestLeaveDetailResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RequestLeaveCommandImpl implements RequestLeaveCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<RequestLeaveDetailResponse> execute(LeaveRequestData request) {
        return filterDate(request)
                .flatMap(newDates -> {
                    request.setDates(newDates);
                    return userRepository.findFirstByUsername(request.getRequester());
                })
                .flatMap(user -> callHelper(request, user)
                        .flatMap(entity -> employeeRepository.findById(user.getEmployeeId())
                                .map(employee -> {
                                    entity.setManager(employee.getManagerUsername());
                                    entity.setDepartmentId(employee.getDepId());
                                    return entity;
                                }))
                )
                .flatMap(leaveRequest -> requestRepository.save(leaveRequest))
                .map(this::createResponse);
    }

    private Mono<List<String>> filterDate(LeaveRequestData request){
        List<String> newDates = new ArrayList<>();
        return Flux.fromIterable(request.getDates())
                .map(this::getDate)
                .flatMap(date -> eventRepository.findFirstByDateAndStatus(date, CalendarStatus.HOLIDAY)
                        .switchIfEmpty(Mono.just(Event.builder().build()))
                        .map(event -> {
                            inputNewDate(event, newDates, date);
                            return date;
                        }))
                .collectList()
                .map(dates -> newDates);
    }

    private void inputNewDate(Event event, List<String> newDates, Date date) {
        String dateString = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(date);
        if (event.getId() != null && !event.getId().isEmpty()){
            if (!newDates.contains(dateString)){
                newDates.add(dateString);
            }
        }
        else if (date.getDay() != 0 && date.getDay() != 6){
            newDates.add(dateString);
        }
    }

    @SneakyThrows
    private Date getDate(String date) {
        return new SimpleDateFormat(DateUtil.DATE_FORMAT).parse(date);
    }

    private Mono<Request> callHelper(LeaveRequestData request, User user) {
        Date currentDate = dateUtil.getNewDate();
        long currentDateTime = currentDate.getTime();
        switch (request.getType()){
            case LeaveTypeConstant.ANNUAL_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(user.getEmployeeId(), LeaveType.annual, currentDate, 0)
                        .switchIfEmpty(Flux.just(Leave.builder().remaining(0).build()))
                        .collectList()
                        .map(leaves -> new AnnualLeaveRequestHelper().processRequest(request, user, leaves, currentDateTime));
            case LeaveTypeConstant.SUBSTITUTE_LEAVE:
                return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(user.getEmployeeId(), LeaveType.substitute, currentDate, 0)
                        .switchIfEmpty(Flux.empty())
                        .collectList()
                        .map(leaves -> new SubstituteLeaveRequestHelper().processRequest(request, user, leaves, currentDateTime));
            case LeaveTypeConstant.EXTRA_LEAVE:
                return leaveRepository.findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(user.getEmployeeId(), LeaveType.extra, currentDate)
                        .switchIfEmpty(Mono.just(Leave.builder().remaining(0).build()))
                        .map(leave -> new ExtraLeaveRequestHelper().processRequest(request, user, leave, currentDateTime));
            case LeaveTypeConstant.CLOSE_FAMILY_DEATH:
            case LeaveTypeConstant.SICK:
                if (request.getDates().size() > 1){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.CHILD_BAPTISM:
            case LeaveTypeConstant.CHILDBIRTH:
            case LeaveTypeConstant.MAIN_FAMILY_DEATH:
                if (request.getDates().size() > 2){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.CHILD_CIRCUMSION:
            case LeaveTypeConstant.MARRIAGE:
                if (request.getDates().size() > 3){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.HAJJ:
                if (request.getDates().size() > 30){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.MATERNITY:
                if (request.getDates().size() > 90){
                    String msg = "dates=EXCEED_ALLOWABLE_QUOTA";
                    throw new IllegalArgumentException(msg);
                }
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            case LeaveTypeConstant.SICK_WITH_MEDICAL_LETTER:
            case LeaveTypeConstant.UNPAID_LEAVE:
                return new SpecialLeaveRequestHelper().processRequest(request, user, currentDateTime);
            default:
                String msg = "type=INVALID_FORMAT";
                throw new IllegalArgumentException(msg);
        }
    }

    private RequestLeaveDetailResponse createResponse(Request request) {
        List<String> dates = new ArrayList<>();
        for (Date dateString : request.getDates()) {
            String date = new SimpleDateFormat(DateUtil.DATE_FORMAT).format(dateString);
            dates.add(date);
        }

        RequestLeaveDetailResponse response = RequestLeaveDetailResponse.builder()
                .files(request.getFiles())
                .dates(dates)
                .notes(request.getNotes())
                .build();
        if (request.getType().equals(RequestType.SPECIAL_LEAVE)){
            response.setType(request.getSpecialLeaveType().toString());
        }
        else {
            response.setType(request.getType().toString());
        }
        response.setId(request.getId());
        response.setCreatedBy(request.getCreatedBy());
        response.setCreatedDate(request.getCreatedDate());

        return response;
    }

}
