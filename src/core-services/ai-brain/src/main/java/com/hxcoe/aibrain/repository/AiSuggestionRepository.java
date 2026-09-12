package com.hxcoe.aibrain.repository;

import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 建议仓储
 */
@Repository
public interface AiSuggestionRepository extends JpaRepository<AiSuggestionEntity, Long> {

    Optional<AiSuggestionEntity> findByEventId(String eventId);

    List<AiSuggestionEntity> findByStatusOrderByCreatedTimeDesc(String status);

    List<AiSuggestionEntity> findBySuggestionTypeOrderByCreatedTimeDesc(String suggestionType);
}
