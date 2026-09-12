package com.hxcoe.agv.repository;

import com.hxcoe.agv.entity.AgvOperationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgvOperationLogRepository extends JpaRepository<AgvOperationLogEntity, Long> {
    List<AgvOperationLogEntity> findByLogTypeOrderByCreateTimeDesc(String logType);
    AgvOperationLogEntity findFirstByLogTypeAndRefId(String logType, String refId);
}

