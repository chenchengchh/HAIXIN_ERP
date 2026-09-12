package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<MaterialEntity, Long>, JpaSpecificationExecutor<MaterialEntity> {
    Page<MaterialEntity> findByMaterialCodeContainingAndMaterialNameContaining(String materialCode, String materialName, Pageable pageable);

    Optional<MaterialEntity> findFirstByMaterialCode(String materialCode);

    long countByCategoryId(Long categoryId);
}
