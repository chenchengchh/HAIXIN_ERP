package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesServiceQualityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesServiceQualityRepository extends JpaRepository<LesServiceQualityEntity, Long> {

    Page<LesServiceQualityEntity> findByPlanId(Long planId, Pageable pageable);
}

