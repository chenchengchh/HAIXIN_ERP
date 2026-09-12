package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AgvDeviceRepository extends JpaRepository<AgvDeviceEntity, Long>, JpaSpecificationExecutor<AgvDeviceEntity> {
    AgvDeviceEntity findByCode(String code);
}
