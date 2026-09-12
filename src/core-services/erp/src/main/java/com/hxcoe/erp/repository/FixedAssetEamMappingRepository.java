package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.FixedAssetEamMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FixedAssetEamMappingRepository extends JpaRepository<FixedAssetEamMappingEntity, Long> {
    Optional<FixedAssetEamMappingEntity> findByFixedAssetId(Long fixedAssetId);
}

