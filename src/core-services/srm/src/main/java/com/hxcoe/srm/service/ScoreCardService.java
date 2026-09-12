package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.ScoreCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ScoreCardService {
    ScoreCardEntity createScoreCard(ScoreCardEntity scoreCard);
    Page<ScoreCardEntity> getScoreCards(Pageable pageable);
    List<ScoreCardEntity> getScoreCardsBySupplierId(Long supplierId);
    Optional<ScoreCardEntity> getScoreCardById(Long id);
    ScoreCardEntity updateScoreCard(Long id, ScoreCardEntity scoreCard);
    void deleteScoreCard(Long id);
}
