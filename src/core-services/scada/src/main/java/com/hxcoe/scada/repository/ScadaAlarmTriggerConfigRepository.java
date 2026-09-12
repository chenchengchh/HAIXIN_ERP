package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaAlarmTriggerConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScadaAlarmTriggerConfigRepository extends JpaRepository<ScadaAlarmTriggerConfigEntity, Long> {
}

