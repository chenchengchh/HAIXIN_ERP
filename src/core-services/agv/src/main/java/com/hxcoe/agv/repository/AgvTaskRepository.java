package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvTaskRepository extends JpaRepository<AgvTaskEntity, Long> {
    AgvTaskEntity findByTaskId(String taskId);
}

