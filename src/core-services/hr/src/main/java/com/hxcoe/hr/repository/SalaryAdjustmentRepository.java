package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.SalaryAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 调薪记录Repository
 */
public interface SalaryAdjustmentRepository extends JpaRepository<SalaryAdjustmentEntity, Long>, JpaSpecificationExecutor<SalaryAdjustmentEntity> {

    /**
     * 根据员工ID查询调薪记录
     * @param employeeId 员工ID
     * @return 调薪记录列表
     */
    List<SalaryAdjustmentEntity> findByEmployeeId(Long employeeId);

    /**
     * 根据状态查询调薪记录
     * @param status 状态
     * @return 调薪记录列表
     */
    List<SalaryAdjustmentEntity> findByStatus(Integer status);
}