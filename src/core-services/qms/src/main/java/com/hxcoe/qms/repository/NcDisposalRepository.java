package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.NcDisposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 不合格品处理仓库
 */
public interface NcDisposalRepository extends JpaRepository<NcDisposalEntity, Long>, JpaSpecificationExecutor<NcDisposalEntity> {
}
