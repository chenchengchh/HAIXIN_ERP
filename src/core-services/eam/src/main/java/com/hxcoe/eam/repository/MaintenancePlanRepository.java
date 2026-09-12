package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.MaintenancePlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlanEntity, Long> {
}
