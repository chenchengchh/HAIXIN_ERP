package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.OptimizationSuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizationSuggestionRepository extends JpaRepository<OptimizationSuggestionEntity, Long> {
    java.util.List<OptimizationSuggestionEntity> findByScheduleResultId(Long scheduleResultId);

    java.util.List<OptimizationSuggestionEntity> findByScheduleResultIdAndType(Long scheduleResultId, String type);

    java.util.List<OptimizationSuggestionEntity> findByScheduleResultIdAndStatus(Long scheduleResultId, String status);

    java.util.List<OptimizationSuggestionEntity> findByScheduleResultIdAndTypeAndStatus(Long scheduleResultId, String type, String status);

    java.util.List<OptimizationSuggestionEntity> findByType(String type);

    java.util.List<OptimizationSuggestionEntity> findByStatus(String status);

    java.util.List<OptimizationSuggestionEntity> findByTypeAndStatus(String type, String status);

    void deleteByScheduleResultIdAndStatus(Long scheduleResultId, String status);
}