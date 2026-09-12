package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.InspectionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 检验结果仓库
 */
public interface InspectionResultRepository extends JpaRepository<InspectionResultEntity, Long>, JpaSpecificationExecutor<InspectionResultEntity> {

    /**
     * 根据结果编号查询
     *
     * @param resultNo 结果编号
     * @return 结果
     */
    Optional<InspectionResultEntity> findByResultNo(String resultNo);
}
