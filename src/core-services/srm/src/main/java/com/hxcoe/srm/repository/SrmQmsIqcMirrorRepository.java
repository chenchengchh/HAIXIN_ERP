package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.SrmQmsIqcMirrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * QMS IQC检验结果幂等镜像仓储
 */
@Repository
public interface SrmQmsIqcMirrorRepository extends JpaRepository<SrmQmsIqcMirrorEntity, Long> {

    /**
     * 按检验单号查询镜像记录（幂等判重）
     *
     * @param inspectionNo QMS检验单号
     * @return 镜像记录
     */
    Optional<SrmQmsIqcMirrorEntity> findByInspectionNo(String inspectionNo);
}
