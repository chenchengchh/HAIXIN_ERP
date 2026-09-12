package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.BenefitConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 福利配置Repository
 */
public interface BenefitConfigRepository extends JpaRepository<BenefitConfigEntity, Long>, JpaSpecificationExecutor<BenefitConfigEntity> {

    /**
     * 根据状态查询福利配置
     * @param status 状态
     * @return 福利配置列表
     */
    List<BenefitConfigEntity> findByStatus(Integer status);
}
