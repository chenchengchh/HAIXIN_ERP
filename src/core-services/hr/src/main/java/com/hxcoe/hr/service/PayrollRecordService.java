package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.PayrollRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 薪资记录Service接口
 */
public interface PayrollRecordService {

    /**
     * 创建薪资记录
     * @param payrollRecord 薪资记录实体
     * @return 薪资记录实体
     */
    PayrollRecordEntity createPayrollRecord(PayrollRecordEntity payrollRecord);

    /**
     * 根据ID查询薪资记录
     * @param id 薪资记录ID
     * @return 薪资记录实体
     */
    PayrollRecordEntity getPayrollRecordById(Long id);

    /**
     * 更新薪资记录
     * @param id 薪资记录ID
     * @param payrollRecord 薪资记录实体
     * @return 薪资记录实体
     */
    PayrollRecordEntity updatePayrollRecord(Long id, PayrollRecordEntity payrollRecord);

    /**
     * 删除薪资记录
     * @param id 薪资记录ID
     */
    void deletePayrollRecord(Long id);

    /**
     * 分页查询薪资记录
     * @param pageable 分页参数
     * @return 薪资记录分页列表
     */
    Page<PayrollRecordEntity> getPayrollRecordsByPage(Pageable pageable);

    /**
     * 根据员工ID查询薪资记录
     * @param employeeId 员工ID
     * @return 薪资记录列表
     */
    List<PayrollRecordEntity> getPayrollRecordsByEmployeeId(Long employeeId);

    /**
     * 根据月份范围查询薪资记录
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 薪资记录列表
     */
    List<PayrollRecordEntity> getPayrollRecordsByMonthRange(String startMonth, String endMonth);

    /**
     * 查询所有薪资记录
     * @return 薪资记录列表
     */
    List<PayrollRecordEntity> getAllPayrollRecords();
}