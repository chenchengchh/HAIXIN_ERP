package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.CapaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * CAPA仓库
 */
public interface CapaRepository extends JpaRepository<CapaEntity, Long>, JpaSpecificationExecutor<CapaEntity> {
}
