package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaActiveAlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScadaActiveAlarmRepository extends JpaRepository<ScadaActiveAlarmEntity, String> {
}

