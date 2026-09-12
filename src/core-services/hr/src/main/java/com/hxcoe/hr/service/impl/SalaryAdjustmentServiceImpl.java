package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.SalaryAdjustmentEntity;
import com.hxcoe.hr.repository.SalaryAdjustmentRepository;
import com.hxcoe.hr.service.SalaryAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 调薪记录Service实现类
 */
@Service
public class SalaryAdjustmentServiceImpl implements SalaryAdjustmentService {

    @Autowired
    private SalaryAdjustmentRepository salaryAdjustmentRepository;

    @Override
    public SalaryAdjustmentEntity createSalaryAdjustment(SalaryAdjustmentEntity salaryAdjustment) {
        return salaryAdjustmentRepository.save(salaryAdjustment);
    }

    @Override
    public SalaryAdjustmentEntity getSalaryAdjustmentById(Long id) {
        return salaryAdjustmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("调薪记录不存在: " + id));
    }

    @Override
    public SalaryAdjustmentEntity updateSalaryAdjustment(Long id, SalaryAdjustmentEntity salaryAdjustment) {
        SalaryAdjustmentEntity existingRecord = getSalaryAdjustmentById(id);
        // 更新调薪记录字段
        existingRecord.setEmployeeId(salaryAdjustment.getEmployeeId());
        existingRecord.setOldSalary(salaryAdjustment.getOldSalary());
        existingRecord.setNewSalary(salaryAdjustment.getNewSalary());
        existingRecord.setReason(salaryAdjustment.getReason());
        existingRecord.setAdjustmentDate(salaryAdjustment.getAdjustmentDate());
        existingRecord.setStatus(salaryAdjustment.getStatus());
        existingRecord.setRemark(salaryAdjustment.getRemark());
        return salaryAdjustmentRepository.save(existingRecord);
    }

    @Override
    public void deleteSalaryAdjustment(Long id) {
        SalaryAdjustmentEntity existingRecord = getSalaryAdjustmentById(id);
        salaryAdjustmentRepository.delete(existingRecord);
    }

    @Override
    public List<SalaryAdjustmentEntity> getAllSalaryAdjustments() {
        return salaryAdjustmentRepository.findAll();
    }

    @Override
    public Page<SalaryAdjustmentEntity> getSalaryAdjustmentsByPage(Pageable pageable) {
        return salaryAdjustmentRepository.findAll(pageable);
    }

    @Override
    public List<SalaryAdjustmentEntity> getSalaryAdjustmentsByEmployeeId(Long employeeId) {
        return salaryAdjustmentRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<SalaryAdjustmentEntity> getSalaryAdjustmentsByStatus(Integer status) {
        return salaryAdjustmentRepository.findByStatus(status);
    }
}