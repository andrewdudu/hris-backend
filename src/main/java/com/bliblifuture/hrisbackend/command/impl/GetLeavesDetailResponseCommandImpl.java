package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesDetailResponseCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.GetLeavesDetailRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveDetailResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GetLeavesDetailResponseCommandImpl implements GetLeavesDetailResponseCommand {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public Mono<List<LeaveDetailResponse>> execute(GetLeavesDetailRequest request) {
        String depCode = "0";
        if (request.getDepartment() != null){
            depCode = request.getDepartment();
        }
        return departmentRepository.findByCode(depCode)
                .switchIfEmpty(Mono.just(Department.builder().build()))
                .flatMap(department -> getRequests(request, department)
                        .switchIfEmpty(Flux.empty())
                        .filter(req -> (!req.getType().equals(RequestType.ATTENDANCE) && !req.getType().equals(RequestType.EXTEND_ANNUAL_LEAVE)))
                        .flatMap(this::createResponse)
                        .collectList()
                );
    }

    private Flux<Request> getRequests(GetLeavesDetailRequest request, Department department) {
        Date currentDate = dateUtil.getNewDate();
        int year = currentDate.getYear() + 1900;

        if (department.getId()!= null && !department.getId().isEmpty()){
            String depId = department.getId();
            if (request.getMonth() > 0){
                Date startOfThisMonth = startOfThisMonth(year, request.getMonth());
                Date endOfThisMonth = getEndOfThisMonth(year, request.getMonth());

                return requestRepository.findByDepartmentIdAndDatesBetweenAndStatus(depId, startOfThisMonth,
                        endOfThisMonth, RequestStatus.APPROVED);
            }

            return requestRepository.findByDepartmentIdAndStatus(depId, RequestStatus.APPROVED);
        }
        else if (request.getMonth() > 0){
            Date startOfThisMonth = startOfThisMonth(year, request.getMonth());
            Date endOfThisMonth = getEndOfThisMonth(year, request.getMonth());

            return requestRepository.findByDatesBetweenAndStatus(startOfThisMonth, endOfThisMonth,
                    RequestStatus.APPROVED);
        }

        String msg = "message=INVALID_REQUEST";
        throw new IllegalArgumentException(msg);
    }

    private Mono<LeaveDetailResponse> createResponse(Request data) {
        String dateString = (data.getCreatedDate().getYear()+1900) + "-" +
                (data.getCreatedDate().getMonth()+1) + "-" + data.getCreatedDate().getDate();

        LeaveDetailResponse response = LeaveDetailResponse.builder()
                .dateString(dateString)
                .notes(data.getNotes())
                .typeLabel(getTypeLabel(data))
                .date(getDateResponse(data))
                .files(data.getFiles())
                .approvedBy(data.getApprovedBy())
                .build();
        response.setId(data.getId());
        response.setCreatedBy(data.getCreatedBy());
        response.setCreatedDate(data.getCreatedDate());
        response.setLastModifiedBy(data.getUpdatedBy());
        response.setLastModifiedDate(data.getUpdatedDate());

        return employeeRepository.findById(data.getEmployeeId())
                .flatMap(this::createEmployeeDetail)
                .map(employee -> {
                    response.setEmployee(employee);
                    return response;
                });
    }

    private Mono<EmployeeDataResponse> createEmployeeDetail(Employee employee) {
        EmployeeDataResponse employeeDataResponse = EmployeeDataResponse.builder()
                .name(employee.getName())
                .nik(employee.getId())
                .organizationUnit(OrganizationUnitResponse.builder().name(employee.getOrganizationUnit()).build())
                .position(PositionResponse.builder().name(employee.getPosition()).build())
                .build();

        return departmentRepository.findById(employee.getDepId())
                .flatMap(department -> {
                    employeeDataResponse.setDepartment(DepartmentResponse.builder().name(department.getName()).build());
                    return officeRepository.findById(employee.getOfficeId());
                })
                .map(office -> {
                    employeeDataResponse.setOffice(OfficeResponse.builder().name(office.getName()).build());
                    return employeeDataResponse;
                });
    }

    private String getTypeLabel(Request data) {
        String typeLabel = "";
        RequestType type = data.getType();
        switch (type){
            case ANNUAL_LEAVE:
            case EXTRA_LEAVE:
            case SUBSTITUTE_LEAVE:
                typeLabel = StringUtils.capitalize(type.toString().replace("_"," ").toLowerCase());
                break;
            case SPECIAL_LEAVE:
                typeLabel = StringUtils.capitalize(data.getSpecialLeaveType().toString().replace("_"," ").toLowerCase());
                break;
        }
        return typeLabel;
    }

    private TimeResponse getDateResponse(Request data) {
        TimeResponse date = TimeResponse.builder()
                .start(data.getDates().get(0))
                .end(data.getDates().get(0))
                .build();
        if (data.getDates().size() > 1){
            for (int i = 1; i < data.getDates().size(); i++) {
                Date theDate = data.getDates().get(i);
                if (theDate.before(date.getStart())){
                    date.setStart(theDate);
                }
                else if (theDate.after(date.getEnd())){
                    date.setEnd(theDate);
                }
            }
        }
        return date;
    }

    @SneakyThrows
    private Date startOfThisMonth(int year, int month){
        return new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse(year + "-" + month + "-1");
    }

    @SneakyThrows
    private Date getEndOfThisMonth(int year, int month){
        int nextMonth = month+1;
        if (month == 12){
            nextMonth = 1;
            year++;
        }
        Date startOfNextMonth = new SimpleDateFormat(DateUtil.DATE_FORMAT)
                .parse(year + "-" + nextMonth + "-1");
        return new Date(startOfNextMonth.getTime() - TimeUnit.SECONDS.toMillis(1));
    }

}
