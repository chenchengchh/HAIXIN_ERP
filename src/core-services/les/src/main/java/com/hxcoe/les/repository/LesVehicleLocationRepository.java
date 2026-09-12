package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesVehicleLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LesVehicleLocationRepository extends JpaRepository<LesVehicleLocationEntity, Long> {

    Optional<LesVehicleLocationEntity> findTopByVehicleIdOrderByRecordTimeDesc(Long vehicleId);

    List<LesVehicleLocationEntity> findByVehicleIdAndRecordTimeBetweenOrderByRecordTimeAsc(
            Long vehicleId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<LesVehicleLocationEntity> findByPlanIdAndRecordTimeBetweenOrderByRecordTimeAsc(
            Long planId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}

