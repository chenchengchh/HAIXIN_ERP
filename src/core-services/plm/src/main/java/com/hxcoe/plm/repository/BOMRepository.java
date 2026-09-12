package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.BOMEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BOMRepository extends JpaRepository<BOMEntity, Long>, JpaSpecificationExecutor<BOMEntity> {
    java.util.List<BOMEntity> findByProductId(Long productId);
}
