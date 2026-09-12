package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.StockCountItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCountItemRepository extends JpaRepository<StockCountItemEntity, Long> {
    List<StockCountItemEntity> findByJobId(Long jobId);
}

