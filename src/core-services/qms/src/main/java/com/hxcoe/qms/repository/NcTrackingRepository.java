package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.NcTrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 不合格品追踪仓库
 */
public interface NcTrackingRepository extends JpaRepository<NcTrackingEntity, Long>, JpaSpecificationExecutor<NcTrackingEntity> {
}
