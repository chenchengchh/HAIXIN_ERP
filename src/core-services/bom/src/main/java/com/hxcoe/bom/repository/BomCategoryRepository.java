package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.BomCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BomCategoryRepository extends JpaRepository<BomCategoryEntity, Long> {
    List<BomCategoryEntity> findByParentId(Long parentId);

    boolean existsByCode(String code);

    Optional<BomCategoryEntity> findFirstByCode(String code);
}

