package com.hxcoe.hr.controller;
import com.hxcoe.hr.entity.AttendanceRecordEntity;
import com.hxcoe.hr.entity.AttendanceExceptionEntity;
import com.hxcoe.hr.entity.LeaveRequestEntity;
import com.hxcoe.hr.service.AttendanceService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理控制器
 * 实现考勤记录、请假申请、考勤异常等API端点
 */
@RestController
@RequestMapping("/api/v1/hr")
@Tag(name = "考勤管理", description = "考勤记录、请假申请、考勤异常等管理相关接口")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    // --- 考勤记录相关API --- 

    /**
     * 获取所有考勤记录
     */
    @GetMapping("/attendance-records")
    @Operation(summary = "获取所有考勤记录", description = "获取所有考勤记录列表")
    public Result<List<AttendanceRecordEntity>> getAllAttendanceRecords() {
        logger.info("获取所有考勤记录");
        List<AttendanceRecordEntity> records = attendanceService.getAllAttendanceRecords();
        return Result.success(records);
    }

    /**
     * 分页获取考勤记录
     */
    @GetMapping("/attendance-records/page")
    @Operation(summary = "分页获取考勤记录", description = "分页查询考勤记录")
    public Result<PageResult<AttendanceRecordEntity>> getAttendanceRecordsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) Map<String, Object> params) {
        
        logger.info("分页获取考勤记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "createdTime"));
        Page<AttendanceRecordEntity> attendanceRecords = attendanceService.getAttendanceRecordsByPage(pageable, params);
        
        PageResult<AttendanceRecordEntity> pageResult = new PageResult<>();
        pageResult.setTotal(attendanceRecords.getTotalElements());
        pageResult.setPageSize(attendanceRecords.getSize());
        pageResult.setCurrentPage(attendanceRecords.getNumber() + 1);
        pageResult.setRecords(attendanceRecords.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据员工ID和日期范围获取考勤记录
     */
    @GetMapping("/attendance-records/by-employee")
    @Operation(summary = "根据员工获取考勤记录", description = "根据员工ID和日期范围获取考勤记录")
    public Result<List<AttendanceRecordEntity>> getAttendanceRecordsByEmployee(
            @RequestParam Long employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        logger.info("根据员工获取考勤记录: employeeId={}, startDate={}, endDate={}", employeeId, startDate, endDate);
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
        List<AttendanceRecordEntity> records = attendanceService.getAttendanceRecordsByEmployeeIdAndDateRange(employeeId, start, end);
        return Result.success(records);
    }

    /**
     * 创建考勤记录
     */
    @PostMapping("/attendance-records")
    @Operation(summary = "创建考勤记录", description = "创建新的考勤记录")
    public Result<AttendanceRecordEntity> createAttendanceRecord(@RequestBody AttendanceRecordEntity attendanceRecord) {
        logger.info("创建考勤记录");
        AttendanceRecordEntity createdRecord = attendanceService.createAttendanceRecord(attendanceRecord);
        return Result.success(createdRecord);
    }

    /**
     * 更新考勤记录
     */
    @PutMapping("/attendance-records/{id}")
    @Operation(summary = "更新考勤记录", description = "更新考勤记录信息")
    public Result<AttendanceRecordEntity> updateAttendanceRecord(
            @PathVariable Long id,
            @RequestBody AttendanceRecordEntity attendanceRecord) {
        logger.info("更新考勤记录: id={}", id);
        AttendanceRecordEntity updatedRecord = attendanceService.updateAttendanceRecord(id, attendanceRecord);
        return Result.success(updatedRecord);
    }

    /**
     * 删除考勤记录
     */
    @DeleteMapping("/attendance-records/{id}")
    @Operation(summary = "删除考勤记录", description = "删除考勤记录")
    public Result<Void> deleteAttendanceRecord(@PathVariable Long id) {
        logger.info("删除考勤记录: id={}", id);
        attendanceService.deleteAttendanceRecord(id);
        return Result.success();
    }

    // --- 请假申请相关API --- 

    /**
     * 获取所有请假申请
     */
    @GetMapping("/leave-requests")
    @Operation(summary = "获取所有请假申请", description = "获取所有请假申请列表")
    public Result<List<LeaveRequestEntity>> getAllLeaveRequests() {
        logger.info("获取所有请假申请");
        List<LeaveRequestEntity> requests = attendanceService.getAllLeaveRequests();
        return Result.success(requests);
    }

    /**
     * 分页获取请假申请
     */
    @GetMapping("/leave-requests/page")
    @Operation(summary = "分页获取请假申请", description = "分页查询请假申请")
    public Result<PageResult<LeaveRequestEntity>> getLeaveRequestsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) Map<String, Object> params) {
        
        logger.info("分页获取请假申请: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "createdTime"));
        Page<LeaveRequestEntity> leaveRequests = attendanceService.getLeaveRequestsByPage(pageable, params);
        
        PageResult<LeaveRequestEntity> pageResult = new PageResult<>();
        pageResult.setTotal(leaveRequests.getTotalElements());
        pageResult.setPageSize(leaveRequests.getSize());
        pageResult.setCurrentPage(leaveRequests.getNumber() + 1);
        pageResult.setRecords(leaveRequests.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 创建请假申请
     */
    @PostMapping("/leave-requests")
    @Operation(summary = "创建请假申请", description = "创建新的请假申请")
    public Result<LeaveRequestEntity> createLeaveRequest(@RequestBody LeaveRequestEntity leaveRequest) {
        logger.info("创建请假申请");
        LeaveRequestEntity createdRequest = attendanceService.createLeaveRequest(leaveRequest);
        return Result.success(createdRequest);
    }

    /**
     * 更新请假申请
     */
    @PutMapping("/leave-requests/{id}")
    @Operation(summary = "更新请假申请", description = "更新请假申请信息")
    public Result<LeaveRequestEntity> updateLeaveRequest(
            @PathVariable Long id,
            @RequestBody LeaveRequestEntity leaveRequest) {
        logger.info("更新请假申请: id={}", id);
        LeaveRequestEntity updatedRequest = attendanceService.updateLeaveRequest(id, leaveRequest);
        return Result.success(updatedRequest);
    }

    /**
     * 删除请假申请
     */
    @DeleteMapping("/leave-requests/{id}")
    @Operation(summary = "删除请假申请", description = "删除请假申请")
    public Result<Void> deleteLeaveRequest(@PathVariable Long id) {
        logger.info("删除请假申请: id={}", id);
        attendanceService.deleteLeaveRequest(id);
        return Result.success();
    }

    /**
     * 审批请假申请
     */
    @PutMapping("/leave-requests/{id}/approve")
    @Operation(summary = "审批请假申请", description = "审批请假申请")
    public Result<LeaveRequestEntity> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        logger.info("审批请假申请: id={}, status={}", id, requestBody.get("status"));
        Integer status = (Integer) requestBody.get("status");
        String remark = (String) requestBody.get("remark");
        LeaveRequestEntity approvedRequest = attendanceService.approveLeaveRequest(id, status, remark);
        return Result.success(approvedRequest);
    }

    // --- 考勤异常相关API --- 

    /**
     * 获取所有考勤异常
     */
    @GetMapping("/attendance-exceptions")
    @Operation(summary = "获取所有考勤异常", description = "获取所有考勤异常列表")
    public Result<List<AttendanceExceptionEntity>> getAllAttendanceExceptions() {
        logger.info("获取所有考勤异常");
        List<AttendanceExceptionEntity> exceptions = attendanceService.getAllAttendanceExceptions();
        return Result.success(exceptions);
    }

    /**
     * 分页获取考勤异常
     */
    @GetMapping("/attendance-exceptions/page")
    @Operation(summary = "分页获取考勤异常", description = "分页查询考勤异常")
    public Result<PageResult<AttendanceExceptionEntity>> getAttendanceExceptionsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) Map<String, Object> params) {
        
        logger.info("分页获取考勤异常: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "createdTime"));
        Page<AttendanceExceptionEntity> attendanceExceptions = attendanceService.getAttendanceExceptionsByPage(pageable, params);
        
        PageResult<AttendanceExceptionEntity> pageResult = new PageResult<>();
        pageResult.setTotal(attendanceExceptions.getTotalElements());
        pageResult.setPageSize(attendanceExceptions.getSize());
        pageResult.setCurrentPage(attendanceExceptions.getNumber() + 1);
        pageResult.setRecords(attendanceExceptions.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 创建考勤异常
     */
    @PostMapping("/attendance-exceptions")
    @Operation(summary = "创建考勤异常", description = "创建新的考勤异常")
    public Result<AttendanceExceptionEntity> createAttendanceException(@RequestBody AttendanceExceptionEntity attendanceException) {
        logger.info("创建考勤异常");
        AttendanceExceptionEntity createdException = attendanceService.createAttendanceException(attendanceException);
        return Result.success(createdException);
    }

    /**
     * 更新考勤异常
     */
    @PutMapping("/attendance-exceptions/{id}")
    @Operation(summary = "更新考勤异常", description = "更新考勤异常信息")
    public Result<AttendanceExceptionEntity> updateAttendanceException(
            @PathVariable Long id,
            @RequestBody AttendanceExceptionEntity attendanceException) {
        logger.info("更新考勤异常: id={}", id);
        AttendanceExceptionEntity updatedException = attendanceService.updateAttendanceException(id, attendanceException);
        return Result.success(updatedException);
    }

    /**
     * 删除考勤异常
     */
    @DeleteMapping("/attendance-exceptions/{id}")
    @Operation(summary = "删除考勤异常", description = "删除考勤异常")
    public Result<Void> deleteAttendanceException(@PathVariable Long id) {
        logger.info("删除考勤异常: id={}", id);
        attendanceService.deleteAttendanceException(id);
        return Result.success();
    }

    /**
     * 审批考勤异常
     */
    @PutMapping("/attendance-exceptions/{id}/approve")
    @Operation(summary = "审批考勤异常", description = "审批考勤异常")
    public Result<AttendanceExceptionEntity> approveAttendanceException(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam Long approverId,
            @RequestParam(required = false) String remark) {
        logger.info("审批考勤异常: id={}, status={}, approverId={}", id, status, approverId);
        AttendanceExceptionEntity approvedException = attendanceService.approveAttendanceException(id, status, approverId, remark);
        return Result.success(approvedException);
    }

    /**
     * 自动检测考勤异常
     */
    @GetMapping("/attendance-exceptions/auto-detect")
    @Operation(summary = "自动检测考勤异常", description = "自动检测考勤异常")
    public Result<List<AttendanceExceptionEntity>> autoDetectAttendanceExceptions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        logger.info("自动检测考勤异常: startDate={}, endDate={}", startDate, endDate);
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        List<AttendanceExceptionEntity> exceptions = attendanceService.autoDetectAttendanceExceptions(start, end);
        return Result.success(exceptions);
    }

    // --- 考勤统计相关API --- 

    /**
     * 获取状态统计
     */
    @GetMapping("/attendance-statistics/status")
    @Operation(summary = "获取状态统计", description = "获取考勤状态统计")
    public Result<Map<String, Object>> getStatusStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId) {
        logger.info("获取状态统计: startDate={}, endDate={}, departmentId={}", startDate, endDate, departmentId);
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        Map<String, Object> statistics = attendanceService.getStatusStatistics(start, end, departmentId);
        return Result.success(statistics);
    }

    /**
     * 获取部门统计
     */
    @GetMapping("/attendance-statistics/department")
    @Operation(summary = "获取部门统计", description = "获取部门考勤统计")
    public Result<List<Map<String, Object>>> getDepartmentStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        logger.info("获取部门统计: startDate={}, endDate={}", startDate, endDate);
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        List<Map<String, Object>> statistics = attendanceService.getDepartmentStatistics(start, end);
        return Result.success(statistics);
    }

    /**
     * 获取月度统计
     */
    @GetMapping("/attendance-statistics/monthly")
    @Operation(summary = "获取月度统计", description = "获取月度考勤统计")
    public Result<List<Map<String, Object>>> getMonthlyStatistics(
            @RequestParam String year,
            @RequestParam(required = false) Long departmentId) {
        logger.info("获取月度统计: year={}, departmentId={}", year, departmentId);
        List<Map<String, Object>> statistics = attendanceService.getMonthlyStatistics(year, departmentId);
        return Result.success(statistics);
    }
}
