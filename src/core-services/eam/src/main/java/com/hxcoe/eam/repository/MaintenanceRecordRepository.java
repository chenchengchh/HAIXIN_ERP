package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.MaintenanceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecordEntity, Long> {
    List<MaintenanceRecordEntity> findByMaintenanceTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
