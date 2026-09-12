package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesTransportCostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LesTransportCostRepository extends JpaRepository<LesTransportCostEntity, Long> {

    Page<LesTransportCostEntity> findByPlanId(Long planId, Pageable pageable);

    Optional<LesTransportCostEntity> findTopByPlanIdOrderByCreateTimeDesc(Long planId);
}

