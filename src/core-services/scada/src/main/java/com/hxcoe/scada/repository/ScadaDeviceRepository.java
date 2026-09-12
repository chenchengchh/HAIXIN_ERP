package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScadaDeviceRepository extends JpaRepository<ScadaDeviceEntity, Long>, JpaSpecificationExecutor<ScadaDeviceEntity> {
}