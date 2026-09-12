package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvCollisionAvoidanceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvCollisionAvoidanceConfigRepository extends JpaRepository<AgvCollisionAvoidanceConfigEntity, Long> {
    AgvCollisionAvoidanceConfigEntity findByConfigKey(String configKey);
}

