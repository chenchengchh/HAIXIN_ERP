package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.InspectionStandardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 检验标准仓库
 */
public interface InspectionStandardRepository extends JpaRepository<InspectionStandardEntity, Long>, JpaSpecificationExecutor<InspectionStandardEntity> {

    /**
     * 根据标准编号查询检验标准
     *
     * @param standardNo 标准编号
     * @return 检验标准
     */
    Optional<InspectionStandardEntity> findByStandardNo(String standardNo);
}
