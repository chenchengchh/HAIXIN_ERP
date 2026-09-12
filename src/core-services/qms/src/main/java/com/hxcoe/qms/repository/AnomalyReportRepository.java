package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.AnomalyReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 质量异常报告仓库
 */
public interface AnomalyReportRepository extends JpaRepository<AnomalyReportEntity, Long>, JpaSpecificationExecutor<AnomalyReportEntity> {

    /**
     * 根据报告编号查询
     *
     * @param reportNo 报告编号
     * @return 报告
     */
    Optional<AnomalyReportEntity> findByReportNo(String reportNo);
}
