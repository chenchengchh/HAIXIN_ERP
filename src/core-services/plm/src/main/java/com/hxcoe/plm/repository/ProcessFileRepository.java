package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ProcessFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProcessFileRepository extends JpaRepository<ProcessFileEntity, Long>, JpaSpecificationExecutor<ProcessFileEntity> {
}

