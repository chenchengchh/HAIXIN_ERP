package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.CostAccountingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CostAccountingRecordRepository extends JpaRepository<CostAccountingRecordEntity, Long>, JpaSpecificationExecutor<CostAccountingRecordEntity> {
}

