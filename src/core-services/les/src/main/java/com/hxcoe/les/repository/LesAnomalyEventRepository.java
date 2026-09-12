package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesAnomalyEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesAnomalyEventRepository extends JpaRepository<LesAnomalyEventEntity, Long> {

    Page<LesAnomalyEventEntity> findByPlanId(Long planId, Pageable pageable);

    Page<LesAnomalyEventEntity> findByVehicleId(Long vehicleId, Pageable pageable);

    Page<LesAnomalyEventEntity> findByHandlingStatus(String handlingStatus, Pageable pageable);

    Page<LesAnomalyEventEntity> findByPlanIdAndHandlingStatus(Long planId, String handlingStatus, Pageable pageable);

    Page<LesAnomalyEventEntity> findByVehicleIdAndHandlingStatus(Long vehicleId, String handlingStatus, Pageable pageable);
}

