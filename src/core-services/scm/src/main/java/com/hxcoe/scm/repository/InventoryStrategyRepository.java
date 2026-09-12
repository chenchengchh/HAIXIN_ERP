package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.InventoryStrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface InventoryStrategyRepository extends JpaRepository<InventoryStrategyEntity, Long>, JpaSpecificationExecutor<InventoryStrategyEntity> {
    Optional<InventoryStrategyEntity> findByMaterialCode(String materialCode);

    List<InventoryStrategyEntity> findByStatus(String status);
}
