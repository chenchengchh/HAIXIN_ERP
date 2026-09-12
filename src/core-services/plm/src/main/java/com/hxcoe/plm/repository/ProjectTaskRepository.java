package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ProjectTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTaskEntity, Long> {
    List<ProjectTaskEntity> findByProjectIdOrderByIdAsc(Long projectId);

    void deleteByProjectId(Long projectId);
}

