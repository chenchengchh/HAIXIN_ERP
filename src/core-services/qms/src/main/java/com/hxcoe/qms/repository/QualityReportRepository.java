package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.QualityReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 质量报告仓库
 */
public interface QualityReportRepository extends JpaRepository<QualityReportEntity, Long>, JpaSpecificationExecutor<QualityReportEntity> {

    /**
     * 根据报告编号查询
     *
     * @param reportNo 报告编号
     * @return 报告
     */
    Optional<QualityReportEntity> findByReportNo(String reportNo);
}
