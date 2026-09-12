package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleResultRepository extends JpaRepository<ScheduleResultEntity, Long> {
    java.util.List<ScheduleResultEntity> findByPlanId(@Param("planId") Long planId);
    
    java.util.List<ScheduleResultEntity> findByStatus(@Param("status") String status);
    
    java.util.List<ScheduleResultEntity> findByPlanIdAndStatus(@Param("planId") Long planId, @Param("status") String status);
    
    Page<ScheduleResultEntity> findByScheduleNo(@Param("scheduleNo") String scheduleNo, Pageable pageable);
    
    Page<ScheduleResultEntity> findByAlgorithm(@Param("algorithm") String algorithm, Pageable pageable);
    
    Page<ScheduleResultEntity> findByScheduleNoAndAlgorithm(@Param("scheduleNo") String scheduleNo, @Param("algorithm") String algorithm, Pageable pageable);
}

