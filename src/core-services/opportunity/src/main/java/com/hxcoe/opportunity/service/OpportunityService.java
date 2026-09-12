package com.hxcoe.opportunity.service;

import com.hxcoe.opportunity.entity.OpportunityEntity;
import com.hxcoe.common.result.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OpportunityService {
    OpportunityEntity createOpportunity(OpportunityEntity opportunity);
    OpportunityEntity updateOpportunity(Long id, OpportunityEntity opportunity);
    void deleteOpportunity(Long id);
    Optional<OpportunityEntity> getOpportunityById(Long id);
    OpportunityEntity getOpportunityByOpportunityCode(String opportunityCode);
    List<OpportunityEntity> getAllOpportunities();
    PageResult<OpportunityEntity> getOpportunitiesByPage(Pageable pageable);
    List<OpportunityEntity> getOpportunitiesByCustomerId(Long customerId);
    List<OpportunityEntity> getOpportunitiesByOwner(String owner);
    List<OpportunityEntity> getOpportunitiesByStage(String stage);
}