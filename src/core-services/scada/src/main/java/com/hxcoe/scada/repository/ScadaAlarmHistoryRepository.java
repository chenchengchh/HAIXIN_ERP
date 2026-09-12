package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaAlarmHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScadaAlarmHistoryRepository extends JpaRepository<ScadaAlarmHistoryEntity, Long>, JpaSpecificationExecutor<ScadaAlarmHistoryEntity> {
}
