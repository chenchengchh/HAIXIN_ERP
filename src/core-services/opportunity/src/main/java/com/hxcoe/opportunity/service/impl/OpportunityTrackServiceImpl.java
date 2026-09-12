package com.hxcoe.opportunity.service.impl;

import com.hxcoe.opportunity.entity.OpportunityTrackEntity;
import com.hxcoe.opportunity.entity.OpportunityEntity;
import com.hxcoe.opportunity.repository.OpportunityTrackRepository;
import com.hxcoe.opportunity.repository.OpportunityRepository;
import com.hxcoe.opportunity.service.OpportunityTrackService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OpportunityTrackServiceImpl implements OpportunityTrackService {
    
    @Autowired
    private OpportunityTrackRepository opportunityTrackRepository;
    
    @Autowired
    private OpportunityRepository opportunityRepository;
    
    @Override
    public OpportunityTrackEntity createOpportunityTrack(OpportunityTrackEntity track) {
        return opportunityTrackRepository.save(track);
    }
    
    @Override
    public OpportunityTrackEntity updateOpportunityTrack(Long id, OpportunityTrackEntity track) {
        Optional<OpportunityTrackEntity> existingTrack = opportunityTrackRepository.findById(id);
        if (existingTrack.isPresent()) {
            OpportunityTrackEntity updatedTrack = existingTrack.get();
            updatedTrack.setOpportunity(track.getOpportunity());
            updatedTrack.setTrackTime(track.getTrackTime());
            updatedTrack.setContent(track.getContent());
            updatedTrack.setTrackType(track.getTrackType());
            updatedTrack.setTracker(track.getTracker());
            updatedTrack.setNextFollowTime(track.getNextFollowTime());
            updatedTrack.setRemark(track.getRemark());
            return opportunityTrackRepository.save(updatedTrack);
        }
        return null;
    }
    
    @Override
    public void deleteOpportunityTrack(Long id) {
        opportunityTrackRepository.deleteById(id);
    }
    
    @Override
    public Optional<OpportunityTrackEntity> getOpportunityTrackById(Long id) {
        return opportunityTrackRepository.findById(id);
    }
    
    @Override
    public List<OpportunityTrackEntity> getAllOpportunityTracks() {
        return opportunityTrackRepository.findAll();
    }
    
    @Override
    public PageResult<OpportunityTrackEntity> getOpportunityTracksByPage(Pageable pageable) {
        Page<OpportunityTrackEntity> page = opportunityTrackRepository.findAll(pageable);
        return PageResult.build(
            page.getTotalElements(),
            page.getSize(),
            page.getNumber() + 1,
            page.getContent()
        );
    }
    
    @Override
    public List<OpportunityTrackEntity> getOpportunityTracksByOpportunityId(Long opportunityId) {
        Optional<OpportunityEntity> opportunity = opportunityRepository.findById(opportunityId);
        if (opportunity.isPresent()) {
            return opportunityTrackRepository.findByOpportunity(opportunity.get());
        }
        return List.of();
    }
    
    @Override
    public List<OpportunityTrackEntity> getOpportunityTracksByTracker(String tracker) {
        return opportunityTrackRepository.findByTracker(tracker);
    }
}