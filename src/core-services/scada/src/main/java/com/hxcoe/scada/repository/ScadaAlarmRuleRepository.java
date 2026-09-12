package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaAlarmRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScadaAlarmRuleRepository extends JpaRepository<ScadaAlarmRuleEntity, Long> {
}

