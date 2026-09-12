package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.SocialSecurityRecordEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.SocialSecurityRecordRepository;
import com.hxcoe.hr.service.SocialSecurityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 社保公积金记录Service实现类
 */
@Service
public class SocialSecurityRecordServiceImpl implements SocialSecurityRecordService {

    /** 员工状态：在职 */
    private static final String EMPLOYEE_STATUS_ACTIVE = "ACTIVE";

    /** 记录状态：未申报 */
    private static final int STATUS_UNDECLARED = 0;

    /** 记录状态：已申报 */
    private static final int STATUS_DECLARED = 1;

    /** 默认缴费基数（薪酬结构无法直接关联员工时统一使用） */
    private static final BigDecimal DEFAULT_BASE_AMOUNT = new BigDecimal("10000");

    /** 养老保险-公司缴费比例 */
    private static final BigDecimal PENSION_COMPANY_RATE = new BigDecimal("0.16");

    /** 养老保险-个人缴费比例 */
    private static final BigDecimal PENSION_PERSONAL_RATE = new BigDecimal("0.08");

    /** 医疗保险-公司缴费比例 */
    private static final BigDecimal MEDICAL_COMPANY_RATE = new BigDecimal("0.095");

    /** 医疗保险-个人缴费比例 */
    private static final BigDecimal MEDICAL_PERSONAL_RATE = new BigDecimal("0.02");

    /** 失业保险-公司缴费比例 */
    private static final BigDecimal UNEMPLOYMENT_COMPANY_RATE = new BigDecimal("0.005");

    /** 失业保险-个人缴费比例 */
    private static final BigDecimal UNEMPLOYMENT_PERSONAL_RATE = new BigDecimal("0.005");

    /** 公积金-公司缴费比例 */
    private static final BigDecimal HOUSING_FUND_COMPANY_RATE = new BigDecimal("0.12");

    /** 公积金-个人缴费比例 */
    private static final BigDecimal HOUSING_FUND_PERSONAL_RATE = new BigDecimal("0.12");

    @Autowired
    private SocialSecurityRecordRepository socialSecurityRecordRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public SocialSecurityRecordEntity createRecord(SocialSecurityRecordEntity record) {
        return socialSecurityRecordRepository.save(record);
    }

    @Override
    public SocialSecurityRecordEntity getRecordById(Long id) {
        return socialSecurityRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("社保公积金记录不存在: " + id));
    }

    @Override
    @Transactional
    public SocialSecurityRecordEntity updateRecord(Long id, SocialSecurityRecordEntity record) {
        SocialSecurityRecordEntity existingRecord = getRecordById(id);
        // 更新社保公积金记录字段
        existingRecord.setEmployeeId(record.getEmployeeId());
        existingRecord.setInsuranceMonth(record.getInsuranceMonth());
        existingRecord.setBaseAmount(record.getBaseAmount());
        existingRecord.setPensionCompany(record.getPensionCompany());
        existingRecord.setPensionPersonal(record.getPensionPersonal());
        existingRecord.setMedicalCompany(record.getMedicalCompany());
        existingRecord.setMedicalPersonal(record.getMedicalPersonal());
        existingRecord.setUnemploymentCompany(record.getUnemploymentCompany());
        existingRecord.setUnemploymentPersonal(record.getUnemploymentPersonal());
        existingRecord.setHousingFundCompany(record.getHousingFundCompany());
        existingRecord.setHousingFundPersonal(record.getHousingFundPersonal());
        existingRecord.setStatus(record.getStatus());
        existingRecord.setRemark(record.getRemark());
        return socialSecurityRecordRepository.save(existingRecord);
    }

    @Override
    public void deleteRecord(Long id) {
        SocialSecurityRecordEntity existingRecord = getRecordById(id);
        socialSecurityRecordRepository.delete(existingRecord);
    }

    @Override
    public List<SocialSecurityRecordEntity> getAllRecords() {
        return socialSecurityRecordRepository.findAll();
    }

    @Override
    public Page<SocialSecurityRecordEntity> getRecordsByPage(Pageable pageable) {
        return socialSecurityRecordRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public int calculateByMonth(String month) {
        // 查询所有在职员工
        List<EmployeeEntity> activeEmployees = employeeRepository.findByStatus(EMPLOYEE_STATUS_ACTIVE);
        int count = 0;
        for (EmployeeEntity employee : activeEmployees) {
            // 若该员工该月记录已存在则跳过
            if (socialSecurityRecordRepository.existsByEmployeeIdAndInsuranceMonth(employee.getId(), month)) {
                continue;
            }
            // 薪酬结构无法直接关联员工，统一使用默认缴费基数
            BigDecimal baseAmount = DEFAULT_BASE_AMOUNT;
            SocialSecurityRecordEntity record = SocialSecurityRecordEntity.builder()
                    .employeeId(employee.getId())
                    .insuranceMonth(month)
                    .baseAmount(baseAmount)
                    .pensionCompany(calcAmount(baseAmount, PENSION_COMPANY_RATE))
                    .pensionPersonal(calcAmount(baseAmount, PENSION_PERSONAL_RATE))
                    .medicalCompany(calcAmount(baseAmount, MEDICAL_COMPANY_RATE))
                    .medicalPersonal(calcAmount(baseAmount, MEDICAL_PERSONAL_RATE))
                    .unemploymentCompany(calcAmount(baseAmount, UNEMPLOYMENT_COMPANY_RATE))
                    .unemploymentPersonal(calcAmount(baseAmount, UNEMPLOYMENT_PERSONAL_RATE))
                    .housingFundCompany(calcAmount(baseAmount, HOUSING_FUND_COMPANY_RATE))
                    .housingFundPersonal(calcAmount(baseAmount, HOUSING_FUND_PERSONAL_RATE))
                    .status(STATUS_UNDECLARED)
                    .build();
            socialSecurityRecordRepository.save(record);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int declareByMonth(String month) {
        // 查询该月所有记录，将未申报的置为已申报
        List<SocialSecurityRecordEntity> records = socialSecurityRecordRepository.findByInsuranceMonth(month);
        int count = 0;
        for (SocialSecurityRecordEntity record : records) {
            if (record.getStatus() != null && record.getStatus() == STATUS_UNDECLARED) {
                record.setStatus(STATUS_DECLARED);
                socialSecurityRecordRepository.save(record);
                count++;
            }
        }
        return count;
    }

    /**
     * 按缴费基数和比例计算缴费金额（保留两位小数）
     * @param baseAmount 缴费基数
     * @param rate 缴费比例
     * @return 缴费金额
     */
    private BigDecimal calcAmount(BigDecimal baseAmount, BigDecimal rate) {
        return baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
