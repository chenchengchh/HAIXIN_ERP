package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.AttendanceRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 考勤记录仓库接口
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecordEntity, Long> {
    
    /**
     * 使用JOIN FETCH获取所有考勤记录，包括员工、部门和职位信息
     */
    @Query("SELECT ar FROM AttendanceRecordEntity ar JOIN FETCH ar.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    Page<AttendanceRecordEntity> findAllWithEmployeeDetails(Pageable pageable);
    
    /**
     * 使用JOIN FETCH获取所有考勤记录，包括员工、部门和职位信息
     */
    @Query("SELECT ar FROM AttendanceRecordEntity ar JOIN FETCH ar.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    java.util.List<AttendanceRecordEntity> findAllWithEmployeeDetails();
}