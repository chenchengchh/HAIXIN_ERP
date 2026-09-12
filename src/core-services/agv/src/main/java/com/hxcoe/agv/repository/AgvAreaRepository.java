package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvAreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvAreaRepository extends JpaRepository<AgvAreaEntity, Long> {
    AgvAreaEntity findByAreaId(String areaId);
}

