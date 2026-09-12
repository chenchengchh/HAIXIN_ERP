package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.ScoreCardEntity;
import com.hxcoe.srm.repository.ScoreCardRepository;
import com.hxcoe.srm.service.ScoreCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreCardServiceImpl implements ScoreCardService {

    @Autowired
    private ScoreCardRepository scoreCardRepository;

    @Override
    public ScoreCardEntity createScoreCard(ScoreCardEntity scoreCard) {
        return scoreCardRepository.save(scoreCard);
    }

    @Override
    public Page<ScoreCardEntity> getScoreCards(Pageable pageable) {
        return scoreCardRepository.findAll(pageable);
    }

    @Override
    public List<ScoreCardEntity> getScoreCardsBySupplierId(Long supplierId) {
        return scoreCardRepository.findBySupplierId(supplierId);
    }

    @Override
    public Optional<ScoreCardEntity> getScoreCardById(Long id) {
        return scoreCardRepository.findById(id);
    }

    @Override
    public ScoreCardEntity updateScoreCard(Long id, ScoreCardEntity scoreCard) {
        return scoreCardRepository.findById(id).map(existing -> {
            existing.setQualityScore(scoreCard.getQualityScore());
            existing.setDeliveryScore(scoreCard.getDeliveryScore());
            existing.setCostScore(scoreCard.getCostScore());
            existing.setServiceScore(scoreCard.getServiceScore());
            existing.setTotalScore(scoreCard.getTotalScore());
            existing.setGrade(scoreCard.getGrade());
            existing.setComments(scoreCard.getComments());
            return scoreCardRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteScoreCard(Long id) {
        scoreCardRepository.deleteById(id);
    }
}
