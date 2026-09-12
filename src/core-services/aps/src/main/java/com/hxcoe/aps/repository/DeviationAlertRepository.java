package com.hxcoe.aps.repository;

import com.hxcoe.aps.entity.DeviationAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviationAlertRepository extends JpaRepository<DeviationAlertEntity, Long> {
    java.util.List<DeviationAlertEntity> findByPlanId(Long planId);
    
    java.util.List<DeviationAlertEntity> findByPlanIdAndSeverity(Long planId, String severity);
    
    java.util.List<DeviationAlertEntity> findByPlanIdAndStatus(Long planId, String status);
    
    java.util.List<DeviationAlertEntity> findByPlanIdAndSeverityAndStatus(Long planId, String severity, String status);
    
    java.util.List<DeviationAlertEntity> findByOrderId(Long orderId);
    
    java.util.List<DeviationAlertEntity> findByTaskId(Long taskId);
}
