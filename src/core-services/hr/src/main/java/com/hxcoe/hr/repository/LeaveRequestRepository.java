package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.LeaveRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 请假申请仓库接口
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {
    
    /**
     * 使用JOIN FETCH获取所有请假申请，包括员工、部门和职位信息
     */
    @Query("SELECT lr FROM LeaveRequestEntity lr JOIN FETCH lr.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    Page<LeaveRequestEntity> findAllWithEmployeeDetails(Pageable pageable);
    
    /**
     * 使用JOIN FETCH获取所有请假申请，包括员工、部门和职位信息
     */
    @Query("SELECT lr FROM LeaveRequestEntity lr JOIN FETCH lr.employee e JOIN FETCH e.department d JOIN FETCH e.position p")
    java.util.List<LeaveRequestEntity> findAllWithEmployeeDetails();
}