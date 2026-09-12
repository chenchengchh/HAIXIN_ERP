package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.StockCountJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCountJobRepository extends JpaRepository<StockCountJobEntity, Long>, JpaSpecificationExecutor<StockCountJobEntity> {
}

