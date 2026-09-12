package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.WorkOrderMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkOrderMaterialRepository extends JpaRepository<WorkOrderMaterialEntity, Long> {
    List<WorkOrderMaterialEntity> findByWorkOrderId(Long workOrderId);
}
