package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.AttendanceExceptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 考勤异常仓库接口
 */
@Repository
public interface AttendanceExceptionRepository extends JpaRepository<AttendanceExceptionEntity, Long> {
    
    /**
     * 使用JOIN FETCH获取所有考勤异常，包括员工、部门和职位信息
     */
    @Query("SELECT ae FROM AttendanceExceptionEntity ae JOIN FETCH ae.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    Page<AttendanceExceptionEntity> findAllWithEmployeeDetails(Pageable pageable);
    
    /**
     * 使用JOIN FETCH获取所有考勤异常，包括员工、部门和职位信息
     */
    @Query("SELECT ae FROM AttendanceExceptionEntity ae JOIN FETCH ae.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    java.util.List<AttendanceExceptionEntity> findAllWithEmployeeDetails();
}