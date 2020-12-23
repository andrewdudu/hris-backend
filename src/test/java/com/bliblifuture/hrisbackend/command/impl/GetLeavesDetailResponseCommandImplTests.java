package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetLeavesDetailResponseCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.Gender;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestStatus;
import com.bliblifuture.hrisbackend.constant.enumerator.RequestType;
import com.bliblifuture.hrisbackend.constant.enumerator.SpecialLeaveType;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.Office;
import com.bliblifuture.hrisbackend.model.entity.Request;
import com.bliblifuture.hrisbackend.model.request.GetLeavesDetailRequest;
import com.bliblifuture.hrisbackend.model.response.LeaveDetailResponse;
import com.bliblifuture.hrisbackend.model.response.util.*;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.repository.RequestRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetLeavesDetailResponseCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetLeavesDetailResponseCommand getLeavesDetailResponseCommand(){
            return new GetLeavesDetailResponseCommandImpl();
        }
    }

    @Autowired
    private GetLeavesDetailResponseCommand getLeavesDetailResponseCommand;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private OfficeRepository officeRepository;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void test_execute() throws ParseException {
        String departmentCode = "DEP-1";
        GetLeavesDetailRequest request =  GetLeavesDetailRequest.builder()
                .department(departmentCode)
                .month(12)
                .build();

        Department department = Department.builder()
                .code("DEP-1")
                .name("InfoTech")
                .build();
        department.setId("ID-1");
        Mockito.when(departmentRepository.findByCode(departmentCode))
                .thenReturn(Mono.just(department));

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-20 09:00:00");
        Mockito.when(dateUtil.getNewDate())
                .thenReturn(currentDate);

        Date startOfThisMonth = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-1");
        Date endOfThisMonth = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-12-31 23:59:59");

        String empId = "EMP-123";
        Employee employee = Employee.builder()
                .name("employee name")
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-12-1"))
                .email("email")
                .depId(department.getId())
                .gender(Gender.MALE)
                .image("image")
                .level("5")
                .managerUsername("manager")
                .officeId("OFFICE-1")
                .organizationUnit("01")
                .position("staff")
                .build();
        employee.setId(empId);

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-12");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-13");
        Request request1 = Request.builder()
                .specialLeaveType(SpecialLeaveType.SICK_WITH_MEDICAL_LETTER)
                .departmentId(department.getId())
                .employeeId(empId)
                .dates(Arrays.asList(date1, date2))
                .status(RequestStatus.APPROVED)
                .type(RequestType.SPECIAL_LEAVE)
                .manager("manager")
                .notes("")
                .approvedBy("admin")
                .files(Arrays.asList("filePath"))
                .build();
        request1.setCreatedDate(date1);

        Date date3 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14");
        Request request2 = Request.builder()
                .departmentId(department.getId())
                .employeeId(empId)
                .dates(Arrays.asList(date3))
                .status(RequestStatus.APPROVED)
                .type(RequestType.ANNUAL_LEAVE)
                .manager("manager")
                .notes("")
                .approvedBy("admin")
                .build();
        request2.setCreatedDate(date3);

        Mockito.when(requestRepository.findByDepartmentIdAndDatesBetweenAndStatus(department.getId(), startOfThisMonth,
                endOfThisMonth, RequestStatus.APPROVED))
                .thenReturn(Flux.just(request1, request2));

        Mockito.when(employeeRepository.findById(empId))
                .thenReturn(Mono.just(employee));

        Mockito.when(departmentRepository.findById(department.getId()))
                .thenReturn(Mono.just(department));

        Office office = Office.builder()
                .name("MAIN OFFICE")
                .code("OFFICE-1")
                .lat(1.1)
                .lon(1.2)
                .build();
        Mockito.when(officeRepository.findById(employee.getOfficeId()))
                .thenReturn(Mono.just(office));

        EmployeeDataResponse employeeResponse = EmployeeDataResponse.builder()
                .nik(empId)
                .position(PositionResponse.builder().name(employee.getPosition()).build())
                .organizationUnit(OrganizationUnitResponse.builder().name(employee.getOrganizationUnit()).build())
                .name(employee.getName())
                .department(DepartmentResponse.builder().name(department.getName()).build())
                .office(OfficeResponse.builder().name(office.getName()).build())
                .build();

        LeaveDetailResponse data1 = LeaveDetailResponse.builder()
                .typeLabel("Sick with medical letter")
                .notes(request1.getNotes())
                .dateString("2020-12-12")
                .date(TimeResponse.builder().start(date1).end(date2).build())
                .approvedBy(request1.getApprovedBy())
                .employee(employeeResponse)
                .files(request1.getFiles())
                .build();

        Date endTime = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-12-14 23:59:59");
        LeaveDetailResponse data2 = LeaveDetailResponse.builder()
                .typeLabel("Annual leave")
                .notes(request2.getNotes())
                .dateString("2020-12-14")
                .date(TimeResponse.builder().start(date3).end(endTime).build())
                .approvedBy(request2.getApprovedBy())
                .employee(employeeResponse)
                .build();

        List<LeaveDetailResponse> expected = Arrays.asList(data1, data2);

        getLeavesDetailResponseCommand.execute(request)
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(departmentRepository, Mockito.times(1)).findByCode(departmentCode);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(requestRepository, Mockito.times(1)).findByDepartmentIdAndDatesBetweenAndStatus(department.getId(), startOfThisMonth,
                endOfThisMonth, RequestStatus.APPROVED);
        Mockito.verify(employeeRepository, Mockito.times(expected.size())).findById(empId);
        Mockito.verify(departmentRepository, Mockito.times(expected.size())).findById(department.getId());
        Mockito.verify(officeRepository, Mockito.times(expected.size())).findById(employee.getOfficeId());

    }
}
