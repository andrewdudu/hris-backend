package com.bliblifuture.hrisbackend.controller;

import com.blibli.oss.command.CommandExecutor;
import com.blibli.oss.common.response.Response;
import com.blibli.oss.common.response.ResponseHelper;
import com.bliblifuture.hrisbackend.command.*;
import com.bliblifuture.hrisbackend.constant.FileConstant;
import com.bliblifuture.hrisbackend.model.request.*;
import com.bliblifuture.hrisbackend.model.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping
public class RequestController {

    @Autowired
    private CommandExecutor commandExecutor;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/attendances")
    public Mono<Response<AttendanceRequestResponse>> requestAttendances(@RequestBody AttendanceRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestAttendanceCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/leaves")
    public Mono<Response<RequestLeaveDetailResponse>> requestLeave(@RequestBody LeaveRequestData requestData, Principal principal){
        requestData.setRequester(principal.getName());
        return commandExecutor.execute(RequestLeaveCommand.class, requestData)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping("/api/request/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> getExtendLeaveData(Principal principal){
        return commandExecutor.execute(GetExtendLeaveDataCommand.class, principal.getName())
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/extend-leave")
    public Mono<Response<ExtendLeaveResponse>> requestExtendLeave(@RequestBody LeaveRequestData request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestExtendLeaveCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/api/requests")
    public Mono<Response<List<RequestResponse>>> getIncomingRequests(@RequestParam("type") String type,
                                                                     @RequestParam(value = "department", required = false) String department,
                                                                     @RequestParam("page") int page,
                                                                     @RequestParam("size") int size, Principal principal){
        GetIncomingRequest request = GetIncomingRequest.builder()
                .type(type).department(department)
                .page(page).size(size)
                .build();
        request.setRequester(principal.getName());
        return commandExecutor.execute(GetIncomingRequestCommand.class, request)
                .map(pagingResponse -> {
                    Response<List<RequestResponse>> response = ResponseHelper.ok(pagingResponse.getData());
                    response.setPaging(pagingResponse.getPaging());
                    return response;
                })
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/api/requests/{id}/_approve")
    public Mono<Response<RequestResponse>> approveRequest(@PathVariable("id") String id, Principal principal){
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());
        return commandExecutor.execute(ApproveRequestCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/api/requests/_approve")
    public Mono<Response<BulkApproveResponse>> bulkApproveRequest(@RequestBody BulkApproveRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(BulkApproveRequestCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/api/requests/{id}/_reject")
    public Mono<Response<RequestResponse>> rejectRequest(@PathVariable("id") String id, Principal principal){
        BaseRequest request = new BaseRequest();
        request.setId(id);
        request.setRequester(principal.getName());
        return commandExecutor.execute(RejectRequestCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping(value = "/api/request/file/image/{filename}", produces = "image/webp")
    public Mono<byte[]> getImage(@PathVariable String filename){
        return commandExecutor.execute(GetFileCommand.class, FileConstant.REQUEST_FILE_PATH + filename)
                .subscribeOn(Schedulers.elastic());
    }

    @GetMapping(value = "/api/request/file/pdf/{filename}", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<byte[]> getPDF(@PathVariable String filename){
        return commandExecutor.execute(GetFileCommand.class, FileConstant.REQUEST_FILE_PATH + filename)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/substitute-leave")
    public Mono<Response<SubstituteLeaveResponse>> addSubstituteLeave(@RequestBody SubstituteLeaveRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(AddSubstituteLeaveCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/api/request/hourly")
    public Mono<Response<HourlyLeaveResponse>> requestHourlyLeave(@RequestBody HourlyLeaveRequest request, Principal principal){
        request.setRequester(principal.getName());
        return commandExecutor.execute(RequestHourlyLeaveCommand.class, request)
                .map(ResponseHelper::ok)
                .subscribeOn(Schedulers.elastic());
    }

}
