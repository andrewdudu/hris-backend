package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.GetAnnouncementCommand;
import com.bliblifuture.hrisbackend.model.request.PagingRequest;
import com.bliblifuture.hrisbackend.model.response.AnnouncementResponse;
import com.bliblifuture.hrisbackend.model.response.PagingResponse;
import com.bliblifuture.hrisbackend.repository.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class GetAnnouncementCommandImpl implements GetAnnouncementCommand {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AttendanceReportRepository attendanceReportRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EventRepository eventRepository;

    @SneakyThrows
    @Override
    public Mono<PagingResponse<AnnouncementResponse>> execute(PagingRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        PagingResponse<AnnouncementResponse> response = new PagingResponse<>();

        LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String year = String.valueOf(localDate.getYear());
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/"+year);

        return eventRepository.findAllByDateAfterOrderByDateDesc(date, pageable)
                .map(events -> events.createResponse(events, new AnnouncementResponse()))
                .collectList()
                .flatMap(announcementResponseList -> {
                    response.setData(announcementResponseList);
                    return eventRepository.countAllByDateAfter(date);
                })
                .map(total -> response.getPagingResponse(request, Math.toIntExact(total)));
    }

}
