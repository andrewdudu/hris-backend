package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.AttendanceLocationType;
import com.bliblifuture.hrisbackend.constant.enumerator.LeaveType;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.model.entity.Attendance;
import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.IncomingRequestResponse;
import com.bliblifuture.hrisbackend.repository.AttendanceRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
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
public class ApproveRequestCommandImpl implements ApproveRequestCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestResponseHelper requestResponseHelper;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private UuidUtil uuidUtil;

    @SneakyThrows
    @Override
    public Mono<IncomingRequestResponse> execute(BaseRequest data) {
        return requestRepository.findById(data.getId())
                .doOnSuccess(this::checkValidity)
                .map(request -> approvedRequest(data, request))
                .flatMap(request -> requestRepository.save(request))
                .flatMap(this::saveEntity)
                .flatMap(request -> requestResponseHelper.createResponse(request));
    }

    private Mono<Request> saveEntity(Request request){
        LeaveType leaveType;
        switch (request.getType()){
            case ATTENDANCE:
                return attendanceRepository.findById(request.getEmployeeId())
                        .flatMap(attendance -> attendanceRepository.save(attendance))
                        .thenReturn(request);
            case EXTEND_ANNUAL_LEAVE:
                return approveExtendAnnualLeave(request);
            case SUBSTITUTE_LEAVE:
                leaveType = LeaveType.substitute;
                return applyLeave(request, leaveType);
            case EXTRA_LEAVE:
                leaveType = LeaveType.extra;
                return applyLeave(request, leaveType);
            case ANNUAL_LEAVE:
                leaveType = LeaveType.annual;
                return applyLeave(request, leaveType);
            default:
                return Mono.just(request);
        }
    }

    private Mono<Request> applyLeave(Request request, LeaveType leaveType) {
        Date currentDate = dateUtil.getNewDate();
        int dayUsed = request.getDates().size();
        return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfter(request.getEmployeeId(), leaveType, currentDate)
                .map(leave -> updateLeave(leave, dayUsed))
                .flatMap(leave -> leaveRepository.save(leave))
                .thenReturn(request);
    }

    private Leave updateLeave(Leave leave, int dayUsed) {
        leave.setRemaining(leave.getRemaining() - dayUsed);
        leave.setUsed(leave.getUsed() + dayUsed);
        return leave;
    }

    private Attendance createAttendance(Request request) {
        Attendance attendance = Attendance.builder()
                .startTime(request.getClockIn())
                .endTime(request.getClockOut())
                .locationType(AttendanceLocationType.REQUESTED)
                .date(request.getDates().get(0))
                .employeeId(request.getEmployeeId())
                .build();
        attendance.setCreatedBy(request.getApprovedBy());
        attendance.setCreatedDate(dateUtil.getNewDate());
        attendance.setId(uuidUtil.getNewID());
        return attendance;
    }

    @SneakyThrows
    private Mono<Request> approveExtendAnnualLeave(Request request) {
        Date currentDate = dateUtil.getNewDate();
        Date newExpDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse((currentDate.getYear()+1901) + "-3-1 00:00:00");
        return leaveRepository.findByEmployeeIdAndTypeAndExpDateAfter(request.getEmployeeId(), LeaveType.annual, currentDate)
                .doOnSuccess(leave -> checkExtensionRequest(leave, currentDate))
                .flatMap(leave -> {
                    leave.setExpDate(newExpDate);
                    return leaveRepository.save(leave);
                })
                .thenReturn(request);
    }

    private void checkExtensionRequest(Leave leave, Date currentDate) {
        if (leave.getRemaining() < 0){
            String errorsMessage = "message=NO_REMAINING_QUOTA";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private void checkValidity(Request request) {
        if (request == null || request.getStatus().equals(RequestStatus.APPROVED) || request.getStatus().equals(RequestStatus.REJECTED)){
            String errorsMessage = "message=NOT_AVAILABLE";
            throw new IllegalArgumentException(errorsMessage);
        }
    }

    private Request approvedRequest(BaseRequest data, Request request) {
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(data.getRequester());
        request.setUpdatedBy(data.getRequester());
        request.setUpdatedDate(dateUtil.getNewDate());

        return request;
    }

}
