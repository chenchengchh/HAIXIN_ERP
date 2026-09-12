package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.AnomalyDisposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 质量异常处置仓库
 */
public interface AnomalyDisposalRepository extends JpaRepository<AnomalyDisposalEntity, Long>, JpaSpecificationExecutor<AnomalyDisposalEntity> {
}
