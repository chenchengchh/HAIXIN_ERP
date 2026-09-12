package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.OptimizationSuggestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OptimizationSuggestionService {

    List<OptimizationSuggestionEntity> getOptimizationSuggestions(Long scheduleResultId, String type, String status);

    List<OptimizationSuggestionEntity> reanalyzeSchedule(Long scheduleResultId);

    OptimizationSuggestionEntity createOptimizationSuggestion(OptimizationSuggestionEntity suggestion);

    OptimizationSuggestionEntity updateOptimizationSuggestion(Long id, OptimizationSuggestionEntity suggestion);

    void deleteOptimizationSuggestion(Long id);

    OptimizationSuggestionEntity acceptSuggestion(Long id);

    OptimizationSuggestionEntity ignoreSuggestion(Long id);

    OptimizationSuggestionEntity resetSuggestionStatus(Long id);

    List<OptimizationSuggestionEntity> getSuggestionReport(Long scheduleResultId);

    Page<OptimizationSuggestionEntity> getOptimizationSuggestionsByPage(Pageable pageable);
}
