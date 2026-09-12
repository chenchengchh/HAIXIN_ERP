package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvCollaborationStrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvCollaborationStrategyRepository extends JpaRepository<AgvCollaborationStrategyEntity, Long> {
    AgvCollaborationStrategyEntity findByStrategyId(String strategyId);
}

