package com.hxcoe.opportunity.service;

import com.hxcoe.opportunity.entity.OpportunityTrackEntity;
import com.hxcoe.common.result.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OpportunityTrackService {
    OpportunityTrackEntity createOpportunityTrack(OpportunityTrackEntity track);
    OpportunityTrackEntity updateOpportunityTrack(Long id, OpportunityTrackEntity track);
    void deleteOpportunityTrack(Long id);
    Optional<OpportunityTrackEntity> getOpportunityTrackById(Long id);
    List<OpportunityTrackEntity> getAllOpportunityTracks();
    PageResult<OpportunityTrackEntity> getOpportunityTracksByPage(Pageable pageable);
    List<OpportunityTrackEntity> getOpportunityTracksByOpportunityId(Long opportunityId);
    List<OpportunityTrackEntity> getOpportunityTracksByTracker(String tracker);
}