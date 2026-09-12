package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.PickingTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickingTaskRepository extends JpaRepository<PickingTaskEntity, Long>, JpaSpecificationExecutor<PickingTaskEntity> {
    Optional<PickingTaskEntity> findByOutboundOrderId(Long outboundOrderId);
    Optional<PickingTaskEntity> findByTaskNo(String taskNo);
}
