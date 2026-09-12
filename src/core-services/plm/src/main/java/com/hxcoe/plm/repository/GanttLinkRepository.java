package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.GanttLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GanttLinkRepository extends JpaRepository<GanttLinkEntity, Long> {
    List<GanttLinkEntity> findByProjectIdOrderByIdAsc(Long projectId);

    void deleteByProjectId(Long projectId);
}

