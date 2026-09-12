package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.BenefitConfigEntity;
import com.hxcoe.hr.entity.BenefitRecordEntity;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.repository.BenefitConfigRepository;
import com.hxcoe.hr.repository.BenefitRecordRepository;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.service.BenefitRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 福利发放记录Service实现类
 */
@Service
public class BenefitRecordServiceImpl implements BenefitRecordService {

    /** 在职员工状态 */
    private static final String EMPLOYEE_STATUS_ACTIVE = "ACTIVE";

    /** 发放记录状态：待发放 */
    private static final int STATUS_PENDING = 0;

    @Autowired
    private BenefitRecordRepository benefitRecordRepository;

    @Autowired
    private BenefitConfigRepository benefitConfigRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public BenefitRecordEntity createBenefitRecord(BenefitRecordEntity benefitRecord) {
        return benefitRecordRepository.save(benefitRecord);
    }

    @Override
    public BenefitRecordEntity getBenefitRecordById(Long id) {
        return benefitRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("福利发放记录不存在: " + id));
    }

    @Override
    public BenefitRecordEntity updateBenefitRecord(Long id, BenefitRecordEntity benefitRecord) {
        BenefitRecordEntity existingRecord = getBenefitRecordById(id);
        // 更新福利发放记录字段
        existingRecord.setBenefitId(benefitRecord.getBenefitId());
        existingRecord.setEmployeeId(benefitRecord.getEmployeeId());
        existingRecord.setDistributeDate(benefitRecord.getDistributeDate());
        existingRecord.setAmount(benefitRecord.getAmount());
        existingRecord.setStatus(benefitRecord.getStatus());
        existingRecord.setRemark(benefitRecord.getRemark());
        return benefitRecordRepository.save(existingRecord);
    }

    @Override
    public void deleteBenefitRecord(Long id) {
        BenefitRecordEntity existingRecord = getBenefitRecordById(id);
        benefitRecordRepository.delete(existingRecord);
    }

    @Override
    public List<BenefitRecordEntity> getAllBenefitRecords() {
        return benefitRecordRepository.findAll();
    }

    @Override
    public Page<BenefitRecordEntity> getBenefitRecordsByPage(Pageable pageable) {
        return benefitRecordRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public int distributeByBenefitId(Long benefitId) {
        // 查询福利配置，不存在则抛异常
        BenefitConfigEntity benefitConfig = benefitConfigRepository.findById(benefitId)
                .orElseThrow(() -> new RuntimeException("福利配置不存在: " + benefitId));
        // 查询所有在职员工
        List<EmployeeEntity> activeEmployees = employeeRepository.findByStatus(EMPLOYEE_STATUS_ACTIVE);
        LocalDate today = LocalDate.now();
        int count = 0;
        // 为每个在职员工生成一条发放记录，同福利+员工+日期已存在则跳过
        for (EmployeeEntity employee : activeEmployees) {
            if (benefitRecordRepository.existsByBenefitIdAndEmployeeIdAndDistributeDate(benefitId, employee.getId(), today)) {
                continue;
            }
            BenefitRecordEntity record = BenefitRecordEntity.builder()
                    .benefitId(benefitId)
                    .employeeId(employee.getId())
                    .distributeDate(today)
                    .amount(benefitConfig.getStandardAmount())
                    .status(STATUS_PENDING)
                    .build();
            benefitRecordRepository.save(record);
            count++;
        }
        return count;
    }
}
