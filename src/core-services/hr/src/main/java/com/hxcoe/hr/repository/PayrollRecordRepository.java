package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.PayrollRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 薪资记录Repository
 */
public interface PayrollRecordRepository extends JpaRepository<PayrollRecordEntity, Long>, JpaSpecificationExecutor<PayrollRecordEntity> {

    /**
     * 根据员工ID和月份查询薪资记录
     * @param employeeId 员工ID
     * @param month 月份
     * @return 薪资记录
     */
    PayrollRecordEntity findByEmployeeIdAndMonth(Long employeeId, String month);

    /**
     * 根据月份范围查询薪资记录
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 薪资记录列表
     */
    List<PayrollRecordEntity> findByMonthBetween(String startMonth, String endMonth);

    /**
     * 根据员工ID查询薪资记录
     * @param employeeId 员工ID
     * @return 薪资记录列表
     */
    List<PayrollRecordEntity> findByEmployeeId(Long employeeId);
}