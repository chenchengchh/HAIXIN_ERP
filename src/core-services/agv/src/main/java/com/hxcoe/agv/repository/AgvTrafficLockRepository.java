package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvTrafficLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgvTrafficLockRepository extends JpaRepository<AgvTrafficLockEntity, Long> {
    List<AgvTrafficLockEntity> findByNodeCodeOrderByLockTimeDesc(String nodeCode);
}

