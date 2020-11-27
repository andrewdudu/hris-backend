package com.bliblifuture.hrisbackend.command.impl.helper;

import com.bliblifuture.hrisbackend.model.entity.Leave;
import com.bliblifuture.hrisbackend.model.entity.User;
import com.bliblifuture.hrisbackend.model.response.UserResponse;
import com.bliblifuture.hrisbackend.model.response.util.LeaveResponse;
import com.bliblifuture.hrisbackend.model.response.util.OfficeResponse;
import com.bliblifuture.hrisbackend.model.response.util.PositionResponse;
import com.bliblifuture.hrisbackend.repository.DepartmentRepository;
import com.bliblifuture.hrisbackend.repository.EmployeeRepository;
import com.bliblifuture.hrisbackend.repository.LeaveRepository;
import com.bliblifuture.hrisbackend.repository.OfficeRepository;
import com.bliblifuture.hrisbackend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class UserResponseHelper {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private DateUtil dateUtil;

    public Mono<UserResponse> getUserResponse(User user) {
        UserResponse userResponse = UserResponse.builder()
                .roles(user.getRoles())
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        return employeeRepository.findById(user.getEmployeeId())
                .flatMap(employee -> {
                    userResponse.setName(employee.getName());
                    userResponse.setJoinDate(employee.getJoinDate());
                    userResponse.setPosition(PositionResponse.builder().name(employee.getPosition()).build());
                    return setDepartmentById(userResponse, employee.getDepId())
                            .flatMap(response -> setOffice(response, employee.getOfficeId()))
                            .flatMap(res -> setTotalRemainingLeaves(res, employee.getId()));
                    }
                );
    }

    private Mono<UserResponse> setOffice(UserResponse response, String id){
        return officeRepository.findById(id)
                .map(office -> {
                    response.setOffice(OfficeResponse.builder().name(office.getName()).build());
                    return response;
                });
    }

    private Mono<UserResponse> setDepartmentById(UserResponse response, String depId){
        return departmentRepository.findById(depId)
                .map(department -> {
                    response.setDepartment(department.getName());
                    return response;
                });
    }

    private Mono<UserResponse> setTotalRemainingLeaves(UserResponse response, String username) {
        return leaveRepository.findByEmployeeIdAndExpDateAfter(username, dateUtil.getNewDate())
                .collectList()
                .map(leaves -> {
                    response.setLeave(LeaveResponse.builder()
                            .remaining(countLeaves(leaves))
                            .build());

                    return response;
                });
    }

    private int countLeaves(List<Leave> leaves) {
        int total = 0;
        for (Leave l : leaves) {
            total += l.getRemaining();
        }
        return total;
    }

}
