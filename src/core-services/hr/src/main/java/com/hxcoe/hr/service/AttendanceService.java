package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.AttendanceRecordEntity;
import com.hxcoe.hr.entity.AttendanceExceptionEntity;
import com.hxcoe.hr.entity.LeaveRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤服务接口
 * 定义考勤记录、请假申请、考勤异常等业务逻辑
 */
public interface AttendanceService {

    // --- 考勤记录相关方法 --- 
    
    /**
     * 获取所有考勤记录
     */
    List<AttendanceRecordEntity> getAllAttendanceRecords();
    
    /**
     * 分页获取考勤记录
     */
    Page<AttendanceRecordEntity> getAttendanceRecordsByPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 根据员工ID和日期范围获取考勤记录
     */
    List<AttendanceRecordEntity> getAttendanceRecordsByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 创建考勤记录
     */
    AttendanceRecordEntity createAttendanceRecord(AttendanceRecordEntity attendanceRecord);
    
    /**
     * 更新考勤记录
     */
    AttendanceRecordEntity updateAttendanceRecord(Long id, AttendanceRecordEntity attendanceRecord);
    
    /**
     * 删除考勤记录
     */
    void deleteAttendanceRecord(Long id);
    
    // --- 请假申请相关方法 --- 
    
    /**
     * 获取所有请假申请
     */
    List<LeaveRequestEntity> getAllLeaveRequests();
    
    /**
     * 分页获取请假申请
     */
    Page<LeaveRequestEntity> getLeaveRequestsByPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建请假申请
     */
    LeaveRequestEntity createLeaveRequest(LeaveRequestEntity leaveRequest);
    
    /**
     * 更新请假申请
     */
    LeaveRequestEntity updateLeaveRequest(Long id, LeaveRequestEntity leaveRequest);
    
    /**
     * 删除请假申请
     */
    void deleteLeaveRequest(Long id);
    
    /**
     * 审批请假申请
     */
    LeaveRequestEntity approveLeaveRequest(Long id, Integer status, String remark);
    
    // --- 考勤异常相关方法 --- 
    
    /**
     * 获取所有考勤异常
     */
    List<AttendanceExceptionEntity> getAllAttendanceExceptions();
    
    /**
     * 分页获取考勤异常
     */
    Page<AttendanceExceptionEntity> getAttendanceExceptionsByPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建考勤异常
     */
    AttendanceExceptionEntity createAttendanceException(AttendanceExceptionEntity attendanceException);
    
    /**
     * 更新考勤异常
     */
    AttendanceExceptionEntity updateAttendanceException(Long id, AttendanceExceptionEntity attendanceException);
    
    /**
     * 删除考勤异常
     */
    void deleteAttendanceException(Long id);
    
    /**
     * 审批考勤异常
     */
    AttendanceExceptionEntity approveAttendanceException(Long id, Integer status, Long approverId, String remark);
    
    /**
     * 自动检测考勤异常
     */
    List<AttendanceExceptionEntity> autoDetectAttendanceExceptions(LocalDate startDate, LocalDate endDate);
    
    // --- 考勤统计相关方法 --- 
    
    /**
     * 获取状态统计
     */
    Map<String, Object> getStatusStatistics(LocalDate startDate, LocalDate endDate, Long departmentId);
    
    /**
     * 获取部门统计
     */
    List<Map<String, Object>> getDepartmentStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取月度统计
     */
    List<Map<String, Object>> getMonthlyStatistics(String year, Long departmentId);
}
