package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesMonitorLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesMonitorLogRepository extends JpaRepository<LesMonitorLogEntity, Long> {

    Page<LesMonitorLogEntity> findByPlanId(Long planId, Pageable pageable);

    Page<LesMonitorLogEntity> findByVehicleId(Long vehicleId, Pageable pageable);

    Page<LesMonitorLogEntity> findByPlanIdAndVehicleId(Long planId, Long vehicleId, Pageable pageable);
}

