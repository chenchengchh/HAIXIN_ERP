package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvTaskAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgvTaskAssignmentRepository extends JpaRepository<AgvTaskAssignmentEntity, Long> {
    List<AgvTaskAssignmentEntity> findByTaskIdOrderByAssignedTimeDesc(String taskId);
}

