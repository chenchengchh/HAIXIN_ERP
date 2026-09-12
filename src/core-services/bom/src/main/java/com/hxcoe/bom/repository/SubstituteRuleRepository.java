package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.SubstituteRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 替代规则配置Repository接口
 */
public interface SubstituteRuleRepository extends JpaRepository<SubstituteRuleEntity, Long>, JpaSpecificationExecutor<SubstituteRuleEntity> {
}
