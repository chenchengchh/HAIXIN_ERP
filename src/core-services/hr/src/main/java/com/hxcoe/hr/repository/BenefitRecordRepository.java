package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.BenefitRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

/**
 * 福利发放记录Repository
 */
public interface BenefitRecordRepository extends JpaRepository<BenefitRecordEntity, Long>, JpaSpecificationExecutor<BenefitRecordEntity> {

    /**
     * 根据福利配置ID查询发放记录
     * @param benefitId 福利配置ID
     * @return 福利发放记录列表
     */
    List<BenefitRecordEntity> findByBenefitId(Long benefitId);

    /**
     * 根据员工ID查询发放记录
     * @param employeeId 员工ID
     * @return 福利发放记录列表
     */
    List<BenefitRecordEntity> findByEmployeeId(Long employeeId);

    /**
     * 判断指定福利、员工、发放日期的记录是否已存在
     * @param benefitId 福利配置ID
     * @param employeeId 员工ID
     * @param distributeDate 发放日期
     * @return 是否存在
     */
    boolean existsByBenefitIdAndEmployeeIdAndDistributeDate(Long benefitId, Long employeeId, LocalDate distributeDate);
}
