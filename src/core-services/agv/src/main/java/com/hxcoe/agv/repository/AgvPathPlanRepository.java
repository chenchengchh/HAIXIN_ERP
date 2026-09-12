package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvPathPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvPathPlanRepository extends JpaRepository<AgvPathPlanEntity, Long> {
    AgvPathPlanEntity findByPlanId(String planId);
}

