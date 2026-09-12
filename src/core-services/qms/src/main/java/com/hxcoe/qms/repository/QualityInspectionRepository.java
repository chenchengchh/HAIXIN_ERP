package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.QualityInspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QualityInspectionRepository extends JpaRepository<QualityInspectionEntity, Long>, JpaSpecificationExecutor<QualityInspectionEntity> {

    /**
     * 按检验单号查询（用于 WMS 触发 IQC 时幂等查重）。
     *
     * @param inspectionCode 检验单号
     * @return 质检单实体（存在时）
     */
    Optional<QualityInspectionEntity> findByInspectionCode(String inspectionCode);
}