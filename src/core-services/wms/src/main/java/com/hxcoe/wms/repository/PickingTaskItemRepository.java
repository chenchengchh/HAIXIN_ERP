package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.PickingTaskItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickingTaskItemRepository extends JpaRepository<PickingTaskItemEntity, Long> {
    List<PickingTaskItemEntity> findByTaskId(Long taskId);
}

