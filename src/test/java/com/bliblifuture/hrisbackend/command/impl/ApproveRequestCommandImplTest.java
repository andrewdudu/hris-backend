package com.bliblifuture.hrisbackend.command.impl;

import com.bliblifuture.hrisbackend.command.ApproveRequestCommand;
import com.bliblifuture.hrisbackend.command.impl.helper.RequestResponseHelper;
import com.bliblifuture.hrisbackend.constant.enumerator.*;
import com.bliblifuture.hrisbackend.model.entity.*;
import com.bliblifuture.hrisbackend.model.request.BaseRequest;
import com.bliblifuture.hrisbackend.model.response.*;
import com.bliblifuture.hrisbackend.model.response.util.RequestDetailResponse;
import com.bliblifuture.hrisbackend.model.response.util.TimeResponse;
import com.bliblifuture.hrisbackend.repository.*;
import com.bliblifuture.hrisbackend.util.DateUtil;
import com.bliblifuture.hrisbackend.util.UuidUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@RunWith(SpringRunner.class)
public class ApproveRequestCommandImplTest {

    @TestConfiguration
    static class command{
        @Bean
        public ApproveRequestCommand approveRequestCommand(){
            return new ApproveRequestCommandImpl();
        }
    }

    @Autowired
    private ApproveRequestCommand approveRequestCommand;

    @MockBean
    private RequestRepository requestRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private LeaveRepository leaveRepository;

    @MockBean
    private RequestResponseHelper requestResponseHelper;

    @MockBean
    private EmployeeLeaveSummaryRepository employeeLeaveSummaryRepository;

    @MockBean
    private DailyAttendanceReportRepository dailyAttendanceReportRepository;

    @MockBean
    private JavaMailSender emailSender;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private UuidUtil uuidUtil;

    @Test
    public void testApproveAttendance_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date start = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 08:15:00");
        Date end = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 17:20:00");

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-20");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.ATTENDANCE)
                .clockIn(start)
                .clockOut(end)
                .dates(Collections.singletonList(date1))
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();
        request.setCreatedDate(date2);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        TimeResponse date = TimeResponse.builder()
                .start(start)
                .end(end)
                .build();
        AttendanceResponse attendanceResponse = AttendanceResponse.builder()
                .date(date)
                .notes(notes)
                .build();
        RequestDetailResponse detail = RequestDetailResponse.builder()
                .attendance(attendanceResponse)
                .build();

        String dateString = (currentDate.getYear() + 1900) + "-" + (currentDate.getMonth() + 1) + "-" + currentDate.getDate();
        String startTime = " 00:00:00";
        Date startOfDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT)
                .parse(dateString + startTime);

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(startOfDate))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(startOfDate)
                .working(1)
                .absent(0)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        String uuid = "ATT123";

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .requestAttendance(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        Mockito.when(uuidUtil.getNewID()).thenReturn(uuid);

        Attendance attendance = Attendance.builder()
                .startTime(approvedRequest.getClockIn())
                .endTime(approvedRequest.getClockOut())
                .locationType(AttendanceLocationType.REQUESTED)
                .date(approvedRequest.getDates().get(0))
                .employeeId(user.getEmployeeId())
                .build();
        attendance.setCreatedBy(approvedRequest.getApprovedBy());
        attendance.setCreatedDate(currentDate);
        attendance.setId(uuid);

        Mockito.when(attendanceRepository.save(attendance))
                .thenReturn(Mono.just(attendance));

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.ATTENDANCE)
                .date(date2)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("blibli");
        mail.setTo(request.getCreatedBy());
        String type = request.getType().toString();
        mail.setSubject(type.replace("_", " ") + " APPROVED");
        mail.setText("Your " + type.toLowerCase().replace("_", " ") + " request has been approved");

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(uuidUtil, Mockito.times(1)).getNewID();
        Mockito.verify(attendanceRepository, Mockito.times(1)).save(attendance);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(approvedRequest);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).findFirstByDate(startOfDate);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1)).save(report);
        Mockito.verify(emailSender, Mockito.times(1)).send(mail);
    }

    @Test
    public void testApproveExtendAnnualLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.EXTEND_ANNUAL_LEAVE)
                .status(RequestStatus.REQUESTED)
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Leave annualLeave = Leave.builder()
                .remaining(2)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-01-01"))
                .used(8)
                .code("ANNUAL")
                .build();
        annualLeave.setId("id");
        Mockito.when(leaveRepository
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(
                        request.getEmployeeId(), LeaveType.annual, currentDate)
        ).thenReturn(Mono.just(annualLeave));

        Date newExpDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2021-3-1 00:00:00");

        Leave newAnnualLeave = Leave.builder().build();
        BeanUtils.copyProperties(annualLeave, newAnnualLeave);
        newAnnualLeave.setExpDate(newExpDate);

        Mockito.when(leaveRepository.save(newAnnualLeave)).thenReturn(Mono.just(newAnnualLeave));

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .annualLeaveExtension(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        ExtendLeaveResponse extend = ExtendLeaveResponse.builder()
                .notes(request.getNotes())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .extend(extend)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.EXTEND)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(leaveRepository, Mockito.times(1)).save(newAnnualLeave);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findFirstByEmployeeIdAndTypeAndExpDateAfterOrderByExpDateAsc(
                        request.getEmployeeId(), LeaveType.annual, currentDate);
        Mockito.verify(requestResponseHelper, Mockito.times(1)).createResponse(approvedRequest);

    }

    @Test
    public void testApproveAnnualLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.ANNUAL_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1))
                .manager("manager")
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Leave annualLeave = Leave.builder()
                .remaining(8)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-01-01"))
                .used(2)
                .code("ANNUAL")
                .build();
        annualLeave.setId("id");
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.annual, currentDate, 0
                )
        ).thenReturn(Flux.just(annualLeave));

        Leave updatedLeave = Leave.builder().build();
        BeanUtils.copyProperties(annualLeave, updatedLeave);
        updatedLeave.setRemaining(7);
        updatedLeave.setUsed(3);

        Mockito.when(leaveRepository.save(updatedLeave)).thenReturn(Mono.just(updatedLeave));

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .annualLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.ANNUAL_LEAVE.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report);
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(leaveRepository, Mockito.times(1)).save(updatedLeave);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.annual, currentDate, 0
                );
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test
    public void testApproveExtraLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.EXTRA_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1))
                .manager("manager")
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Leave extraLeave = Leave.builder()
                .remaining(1)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-01-01"))
                .used(1)
                .code("EXTRA")
                .build();
        extraLeave.setId("id");
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.extra, currentDate, 0
                )
        ).thenReturn(Flux.just(extraLeave));

        Leave updatedLeave = Leave.builder().build();
        BeanUtils.copyProperties(extraLeave, updatedLeave);
        updatedLeave.setRemaining(0);
        updatedLeave.setUsed(2);

        Mockito.when(leaveRepository.save(updatedLeave)).thenReturn(Mono.just(updatedLeave));

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .extraLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.EXTRA_LEAVE.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report);
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(leaveRepository, Mockito.times(1)).save(updatedLeave);
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.extra, currentDate, 0
                );
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test
    public void testApproveSubstituteLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Date date2 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-23");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.SUBSTITUTE_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1, date2))
                .manager("manager")
                .employeeId(user.getEmployeeId())
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Leave subsLeave1 = Leave.builder()
                .remaining(1)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-01-01"))
                .used(0)
                .code("SUBSTITUTE")
                .build();
        subsLeave1.setId("id1");

        Leave subsLeave2 = Leave.builder()
                .remaining(1)
                .expDate(new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2021-01-01"))
                .used(0)
                .code("SUBSTITUTE")
                .build();
        subsLeave2.setId("id2");
        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.substitute, currentDate, 0
                )
        ).thenReturn(Flux.just(subsLeave1, subsLeave2));

        Leave updatedLeave1 = Leave.builder().build();
        BeanUtils.copyProperties(subsLeave1, updatedLeave1);
        updatedLeave1.setRemaining(0);
        updatedLeave1.setUsed(1);

        Leave updatedLeave2 = Leave.builder().build();
        BeanUtils.copyProperties(subsLeave2, updatedLeave2);
        updatedLeave2.setRemaining(0);
        updatedLeave2.setUsed(1);

        Mockito.when(leaveRepository.save(updatedLeave1)).thenReturn(Mono.just(updatedLeave1));
        Mockito.when(leaveRepository.save(updatedLeave2)).thenReturn(Mono.just(updatedLeave2));

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .substituteLeave(2)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date2))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report1 = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report1.setCreatedBy("SYSTEM");
        report1.setCreatedDate(currentDate);
        report1.setUpdatedBy("SYSTEM");
        report1.setUpdatedDate(currentDate);
        report1.setId("DAR" + report1.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report1))
                .thenReturn(Mono.just(report1));

        DailyAttendanceReport report2 = DailyAttendanceReport.builder()
                .date(date2)
                .working(0)
                .absent(1)
                .build();
        report2.setCreatedBy("SYSTEM");
        report2.setCreatedDate(currentDate);
        report2.setUpdatedBy("SYSTEM");
        report2.setUpdatedDate(currentDate);
        report2.setId("DAR" + report2.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report2))
                .thenReturn(Mono.just(report2));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.
                        asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.SUBSTITUTE_LEAVE.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date2);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report2);
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(leaveRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.substitute, currentDate, 0
                );
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test(expected = Exception.class)
    public void testApproveQuotaNotAvailable_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.EXTRA_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1))
                .manager("manager")
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(leaveRepository
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.extra, currentDate, 0
                )
        ).thenReturn(Flux.empty());

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .extraLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.EXTRA_LEAVE.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(0))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(0))
                .save(report);
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(0))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(0))
                .save(leaveSummary);
        Mockito.verify(leaveRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(leaveRepository, Mockito.times(1))
                .findByEmployeeIdAndTypeAndExpDateAfterAndRemainingGreaterThanOrderByExpDate(
                        request.getEmployeeId(), LeaveType.extra, currentDate, 0
                );
        Mockito.verify(requestResponseHelper, Mockito.times(0))
                .createResponse(approvedRequest);

    }

    @Test
    public void testApproveSick_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.SICK)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1))
                .manager("manager")
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .sick(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(SpecialLeaveType.SICK.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report);
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test
    public void testApproveUnpaidLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.SPECIAL_LEAVE)
                .specialLeaveType(SpecialLeaveType.UNPAID_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date1))
                .manager("manager")
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .unpaidLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Mockito.when(dailyAttendanceReportRepository.findFirstByDate(date1))
                .thenReturn(Mono.empty());

        DailyAttendanceReport report = DailyAttendanceReport.builder()
                .date(date1)
                .working(0)
                .absent(1)
                .build();
        report.setCreatedBy("SYSTEM");
        report.setCreatedDate(currentDate);
        report.setUpdatedBy("SYSTEM");
        report.setUpdatedDate(currentDate);
        report.setId("DAR" + report.getDate().getTime());

        Mockito.when(dailyAttendanceReportRepository.save(report))
                .thenReturn(Mono.just(report));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(SpecialLeaveType.UNPAID_LEAVE.toString())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .findFirstByDate(date1);
        Mockito.verify(dailyAttendanceReportRepository, Mockito.times(1))
                .save(report);
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test
    public void testApproveHourlyLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        String notes = "notes";

        Date date1 = new SimpleDateFormat(DateUtil.DATE_FORMAT).parse("2020-10-22");
        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.HOURLY_LEAVE)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date))
                .manager("manager")
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 08:00:00"))
                .endTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 10:00:00"))
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .hourlyLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.HOURLY_LEAVE.toString())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(1)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(leaveSummary);
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(approvedRequest);

    }

    @Test(expected = Exception.class)
    public void testApproveNotAvailable_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 10:00:00");

        String notes = "notes";

        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.HOURLY_LEAVE)
                .status(RequestStatus.APPROVED)
                .dates(Arrays.asList(date))
                .manager("manager")
                .startTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 08:00:00"))
                .endTime(new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 10:00:00"))
                .notes(notes)
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        UserResponse userResponse = UserResponse.builder()
                .username(user.getUsername())
                .employeeId(user.getEmployeeId())
                .build();

        String year = String.valueOf(currentDate.getYear()+1900);
        Mockito.when(employeeLeaveSummaryRepository.findFirstByYearAndEmployeeId(year, request.getEmployeeId()))
                .thenReturn(Mono.empty());

        EmployeeLeaveSummary leaveSummary = EmployeeLeaveSummary.builder()
                .year(year)
                .employeeId(user.getEmployeeId())
                .hourlyLeave(1)
                .build();

        leaveSummary.setId("ELS-" + leaveSummary.getEmployeeId() + "-" + leaveSummary.getYear());
        leaveSummary.setCreatedBy("SYSTEM");
        leaveSummary.setUpdatedBy("SYSTEM");
        leaveSummary.setCreatedDate(currentDate);
        leaveSummary.setUpdatedDate(currentDate);

        Mockito.when(employeeLeaveSummaryRepository.save(leaveSummary))
                .thenReturn(Mono.just(leaveSummary));

        Request approvedRequest = new Request();
        BeanUtils.copyProperties(request, approvedRequest);
        approvedRequest.setStatus(RequestStatus.APPROVED);
        approvedRequest.setUpdatedDate(currentDate);
        approvedRequest.setUpdatedBy(admin.getUsername());
        approvedRequest.setApprovedBy(admin.getUsername());

        Mockito.when(requestRepository.save(approvedRequest))
                .thenReturn(Mono.just(approvedRequest));

        RequestLeaveDetailResponse leaveDetail = RequestLeaveDetailResponse.builder()
                .dates(Arrays.asList("2020-10-22"))
                .notes(request.getNotes())
                .type(RequestType.HOURLY_LEAVE.toString())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        RequestDetailResponse detail = RequestDetailResponse.builder()
                .leave(leaveDetail)
                .build();

        RequestResponse expected = RequestResponse.builder()
                .user(userResponse)
                .status(RequestStatus.APPROVED)
                .type(RequestType.LEAVE)
                .date(date)
                .detail(detail)
                .approvedby(admin.getUsername())
                .build();
        Mockito.when(requestResponseHelper.createResponse(approvedRequest))
                .thenReturn(Mono.just(expected));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe(response -> {
                    Assert.assertEquals(expected, response);
                });

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(0)).save(approvedRequest);
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(0))
                .findFirstByYearAndEmployeeId(year, request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(0))
                .save(leaveSummary);
        Mockito.verify(requestResponseHelper, Mockito.times(0))
                .createResponse(approvedRequest);

    }

    @Test(expected = RuntimeException.class)
    public void testApproveInvalidLeave_execute() throws ParseException {
        User user = User.builder().employeeId("id123").username("username1").build();
        User admin = User.builder().username("admin").build();

        Date date = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-21 07:50:00");

        Date currentDate = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT).parse("2020-10-20 10:00:00");

        Request request = Request.builder()
                .employeeId(user.getEmployeeId())
                .type(RequestType.SET_HOLIDAY)
                .status(RequestStatus.REQUESTED)
                .dates(Arrays.asList(date))
                .build();
        request.setCreatedDate(date);

        Mockito.when(dateUtil.getNewDate()).thenReturn(currentDate);

        Mockito.when(requestRepository.findById(request.getId()))
                .thenReturn(Mono.just(request));

        BaseRequest reqData = new BaseRequest();
        reqData.setId(request.getId());
        reqData.setRequester(admin.getUsername());

        approveRequestCommand.execute(reqData)
                .subscribe();

        Mockito.verify(requestRepository, Mockito.times(1)).findById(request.getId());
        Mockito.verify(requestRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(dateUtil, Mockito.times(1)).getNewDate();
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .findFirstByYearAndEmployeeId(Mockito.anyString(), request.getEmployeeId());
        Mockito.verify(employeeLeaveSummaryRepository, Mockito.times(1))
                .save(Mockito.any());
        Mockito.verify(requestResponseHelper, Mockito.times(1))
                .createResponse(Mockito.any());

    }

}
