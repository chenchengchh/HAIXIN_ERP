package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.AnomalyAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 质量异常分析仓库
 */
public interface AnomalyAnalysisRepository extends JpaRepository<AnomalyAnalysisEntity, Long>, JpaSpecificationExecutor<AnomalyAnalysisEntity> {
}
