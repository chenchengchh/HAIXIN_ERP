package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesTransportTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesTransportTaskRepository extends JpaRepository<LesTransportTaskEntity, Long> {

    LesTransportTaskEntity findByTaskNo(String taskNo);

    Page<LesTransportTaskEntity> findByPlanId(Long planId, Pageable pageable);

    Page<LesTransportTaskEntity> findByStatus(String status, Pageable pageable);

    Page<LesTransportTaskEntity> findByPlanIdAndStatus(Long planId, String status, Pageable pageable);
}

