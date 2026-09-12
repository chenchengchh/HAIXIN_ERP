package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.PayrollRecordEntity;
import com.hxcoe.hr.repository.PayrollRecordRepository;
import com.hxcoe.hr.service.PayrollRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 薪资记录Service实现类
 */
@Service
public class PayrollRecordServiceImpl implements PayrollRecordService {

    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    @Override
    public PayrollRecordEntity createPayrollRecord(PayrollRecordEntity payrollRecord) {
        return payrollRecordRepository.save(payrollRecord);
    }

    @Override
    public PayrollRecordEntity getPayrollRecordById(Long id) {
        return payrollRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("薪资记录不存在: " + id));
    }

    @Override
    public PayrollRecordEntity updatePayrollRecord(Long id, PayrollRecordEntity payrollRecord) {
        PayrollRecordEntity existingRecord = getPayrollRecordById(id);
        // 更新薪资记录字段
        existingRecord.setEmployeeId(payrollRecord.getEmployeeId());
        existingRecord.setMonth(payrollRecord.getMonth());
        existingRecord.setBasicSalary(payrollRecord.getBasicSalary());
        existingRecord.setPerformanceSalary(payrollRecord.getPerformanceSalary());
        existingRecord.setBonus(payrollRecord.getBonus());
        existingRecord.setAllowance(payrollRecord.getAllowance());
        existingRecord.setDeduction(payrollRecord.getDeduction());
        existingRecord.setActualSalary(payrollRecord.getActualSalary());
        existingRecord.setStatus(payrollRecord.getStatus());
        existingRecord.setRemark(payrollRecord.getRemark());
        return payrollRecordRepository.save(existingRecord);
    }

    @Override
    public void deletePayrollRecord(Long id) {
        PayrollRecordEntity existingRecord = getPayrollRecordById(id);
        payrollRecordRepository.delete(existingRecord);
    }

    @Override
    public Page<PayrollRecordEntity> getPayrollRecordsByPage(Pageable pageable) {
        return payrollRecordRepository.findAll(pageable);
    }

    @Override
    public List<PayrollRecordEntity> getPayrollRecordsByEmployeeId(Long employeeId) {
        return payrollRecordRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<PayrollRecordEntity> getPayrollRecordsByMonthRange(String startMonth, String endMonth) {
        return payrollRecordRepository.findByMonthBetween(startMonth, endMonth);
    }

    @Override
    public List<PayrollRecordEntity> getAllPayrollRecords() {
        return payrollRecordRepository.findAll();
    }
}