package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.NcReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 不合格品评审仓库
 */
public interface NcReviewRepository extends JpaRepository<NcReviewEntity, Long>, JpaSpecificationExecutor<NcReviewEntity> {
}
