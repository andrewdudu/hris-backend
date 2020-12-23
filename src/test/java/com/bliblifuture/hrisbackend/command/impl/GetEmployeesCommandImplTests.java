package com.bliblifuture.hrisbackend.command.impl;

import com.blibli.oss.common.paging.Paging;
import com.bliblifuture.hrisbackend.command.GetEmployeesCommand;
import com.bliblifuture.hrisbackend.constant.enumerator.Gender;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.entity.Employee;
import com.bliblifuture.hrisbackend.model.entity.EmployeeIndex;
import com.bliblifuture.hrisbackend.model.entity.Office;
import com.bliblifuture.hrisbackend.model.request.EmployeesRequest;
import com.bliblifuture.hrisbackend.model.response.EmployeeResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeElasticsearchRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
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
import java.util.List;

@RunWith(SpringRunner.class)
public class GetEmployeesCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetEmployeesCommand getEmployeesCommand(){
            return new GetEmployeesCommandImpl();
        }
    }

    @Autowired
    private GetEmployeesCommand getEmployeesCommand;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private OfficeRepository officeRepository;

    @MockBean
    private EmployeeElasticsearchRepository employeeElasticsearchRepository;

    @Test
    public void test_execute() throws ParseException {
        String depId = "DEP-1";
        String name = "a";
        int page = 0;
        int size = 10;
        EmployeesRequest request = new EmployeesRequest();
        request.setDepartment(depId);
        request.setName(name);
        request.setPage(page);
        request.setSize(size);

        Department department = Department.builder()
                .name("InfoTech")
                .code("DEP-1")
                .build();
        department.setId(depId);

        Mockito.when(departmentRepository.findByCode(request.getDepartment()))
                .thenReturn(Mono.just(department));

        Employee employee1 = Employee.builder()
                .position("Staff")
                .organizationUnit("01")
                .officeId("OFFICE-1")
                .managerUsername("manager")
                .level("3")
                .image("image")
                .gender(Gender.MALE)
                .depId("DEP-1")
                .email("email")
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2017-12-12"))
                .name("Employee 1")
                .build();
        employee1.setId("id123");

        Employee employee2 = Employee.builder()
                .position("Staff")
                .organizationUnit("02")
                .officeId("OFFICE-1")
                .managerUsername("manager")
                .level("5")
                .image("image")
                .gender(Gender.FEMALE)
                .depId("DEP-1")
                .email("email")
                .joinDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2016-12-12"))
                .name("Employee 2")
                .build();
        employee2.setId("id345");

        EmployeeIndex employeeIndex1 = EmployeeIndex.builder()
                .id(employee1.getId())
                .departmentId(employee1.getDepId())
                .name(employee1.getName())
                .build();

        EmployeeIndex employeeIndex2 = EmployeeIndex.builder()
                .id(employee2.getId())
                .departmentId(employee2.getDepId())
                .name(employee2.getName())
                .build();

        Mockito.when(employeeElasticsearchRepository
                .search("*a*", "DEP1"))
                .thenReturn(Flux.just(employeeIndex1, employeeIndex2));

        Mockito.when(employeeRepository.findById(employeeIndex1.getId()))
                .thenReturn(Mono.just(employee1));

        Mockito.when(employeeRepository.findById(employeeIndex2.getId()))
                .thenReturn(Mono.just(employee2));

        Office office = Office.builder().name("MAIN OFFICE").code("OFFICE-1").lat(1.1).lon(2.1).build();
        office.setId("OFFICE-1");

        Mockito.when(officeRepository.findById(employee1.getOfficeId()))
                .thenReturn(Mono.just(office));
        Mockito.when(officeRepository.findById(employee2.getOfficeId()))
                .thenReturn(Mono.just(office));

        Mockito.when(departmentRepository.findById(employee1.getDepId()))
                .thenReturn(Mono.just(department));
        Mockito.when(departmentRepository.findById(employee2.getDepId()))
                .thenReturn(Mono.just(department));

        EmployeeResponse data1 = EmployeeResponse.builder()
                .id(employee1.getId())
                .department(department.getName())
                .name(employee1.getName())
                .gender(employee1.getGender())
                .office(OfficeResponse.builder().name(office.getName()).build())
                .build();

        EmployeeResponse data2 = EmployeeResponse.builder()
                .id(employee2.getId())
                .department(department.getName())
                .name(employee2.getName())
                .gender(employee2.getGender())
                .office(OfficeResponse.builder().name(office.getName()).build())
                .build();

        List<EmployeeResponse> data = Arrays.asList(data1, data2);

        Paging paging = Paging.builder()
                .page(request.getPage())
                .itemPerPage(request.getSize())
                .totalItem(data.size())
                .totalPage(1)
                .build();

        PagingResponse<EmployeeResponse> expected = new PagingResponse<>();
        expected.setData(data);
        expected.setPaging(paging);

        getEmployeesCommand.execute(request)
                .subscribe(response -> {
                    Assert.assertEquals(expected.getPaging(), response.getPaging());
                    for (int i = 0; i < expected.getData().size(); i++) {
                        Assert.assertEquals(expected.getData().get(i), response.getData().get(i));
                    }
                });

        Mockito.verify(departmentRepository, Mockito.times(1)).findByCode(request.getDepartment());
        Mockito.verify(employeeElasticsearchRepository, Mockito.times(1)).search("*a*", "DEP1");
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(employeeIndex1.getId());
        Mockito.verify(employeeRepository, Mockito.times(1)).findById(employeeIndex2.getId());
        Mockito.verify(officeRepository, Mockito.times(2)).findById(office.getId());
        Mockito.verify(departmentRepository, Mockito.times(2)).findById(depId);

    }
}
