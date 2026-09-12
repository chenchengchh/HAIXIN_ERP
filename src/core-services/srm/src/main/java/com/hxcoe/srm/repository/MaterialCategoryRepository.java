package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.MaterialCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialCategoryRepository extends JpaRepository<MaterialCategoryEntity, Long> {
}

