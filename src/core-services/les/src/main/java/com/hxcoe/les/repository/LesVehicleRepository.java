package com.hxcoe.les.repository;

import com.hxcoe.les.entity.LesVehicleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LesVehicleRepository extends JpaRepository<LesVehicleEntity, Long> {

    Page<LesVehicleEntity> findByStatus(String status, Pageable pageable);

    Page<LesVehicleEntity> findByLicensePlateContainingIgnoreCase(String keyword, Pageable pageable);

    Page<LesVehicleEntity> findByStatusAndLicensePlateContainingIgnoreCase(String status, String keyword, Pageable pageable);
}

