package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.TransferRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 转岗记录Repository
 */
public interface TransferRecordRepository extends JpaRepository<TransferRecordEntity, Long>, JpaSpecificationExecutor<TransferRecordEntity> {

    /**
     * 根据员工ID查询转岗记录
     * @param employeeId 员工ID
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> findByEmployeeId(Long employeeId);

    /**
     * 根据部门ID查询转岗记录
     * @param departmentId 部门ID
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> findByOldDepartmentIdOrNewDepartmentId(Long departmentId, Long departmentId2);

    /**
     * 根据状态查询转岗记录
     * @param status 状态
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> findByStatus(Integer status);
}