package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvPathPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgvPathPointRepository extends JpaRepository<AgvPathPointEntity, Long> {
    List<AgvPathPointEntity> findByPlanIdOrderBySeqNoAsc(String planId);
}

