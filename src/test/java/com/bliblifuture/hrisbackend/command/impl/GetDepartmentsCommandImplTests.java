package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetDepartmentsCommand;
import com.bliblifuture.hrisbackend.model.entity.Department;
import com.bliblifuture.hrisbackend.model.response.util.DepartmentResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
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

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
public class GetDepartmentsCommandImplTests {

    @TestConfiguration
    static class command{
        @Bean
        public GetDepartmentsCommand getDepartmentsCommand(){
            return new GetDepartmentsCommandImpl();
        }
    }

    @Autowired
    private GetDepartmentsCommand getDepartmentsCommand;

    @MockBean
    private DepartmentRepository departmentRepository;

    @Test
    public void test_execute() {
        Department department1 = Department.builder()
                .name("Information Technology")
                .code("DEP-1")
                .build();
        Department department2 = Department.builder()
                .name("Human Resources")
                .code("DEP-2")
                .build();

        Mockito.when(departmentRepository.findAll())
                .thenReturn(Flux.just(department1, department2));

        DepartmentResponse dep1 = DepartmentResponse.builder()
                .name("Information Technology")
                .code("DEP-1")
                .build();
        DepartmentResponse dep2 = DepartmentResponse.builder()
                .name("Human Resources")
                .code("DEP-2")
                .build();

        List<DepartmentResponse> expected = Arrays.asList(dep1, dep2);

        getDepartmentsCommand.execute("")
                .subscribe(response -> {
                    for (int i = 0; i < expected.size(); i++) {
                        Assert.assertEquals(expected.get(i), response.get(i));
                    }
                });

        Mockito.verify(departmentRepository, Mockito.times(1)).findAll();
    }
}
