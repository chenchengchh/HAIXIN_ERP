package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.SocialSecurityRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 社保公积金记录Repository
 */
public interface SocialSecurityRecordRepository extends JpaRepository<SocialSecurityRecordEntity, Long>, JpaSpecificationExecutor<SocialSecurityRecordEntity> {

    /**
     * 根据缴费月份查询社保公积金记录
     * @param insuranceMonth 缴费月份（YYYY-MM）
     * @return 社保公积金记录列表
     */
    List<SocialSecurityRecordEntity> findByInsuranceMonth(String insuranceMonth);

    /**
     * 根据员工ID查询社保公积金记录
     * @param employeeId 员工ID
     * @return 社保公积金记录列表
     */
    List<SocialSecurityRecordEntity> findByEmployeeId(Long employeeId);

    /**
     * 判断指定员工指定月份的社保公积金记录是否已存在
     * @param employeeId 员工ID
     * @param insuranceMonth 缴费月份（YYYY-MM）
     * @return 存在返回true，否则返回false
     */
    boolean existsByEmployeeIdAndInsuranceMonth(Long employeeId, String insuranceMonth);
}
