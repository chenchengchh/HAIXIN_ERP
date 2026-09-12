package com.hxcoe.opportunity.service.impl;

import com.hxcoe.opportunity.entity.OpportunityEntity;
import com.hxcoe.opportunity.repository.OpportunityRepository;
import com.hxcoe.opportunity.service.OpportunityService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OpportunityServiceImpl implements OpportunityService {
    
    @Autowired
    private OpportunityRepository opportunityRepository;
    
    @Override
    public OpportunityEntity createOpportunity(OpportunityEntity opportunity) {
        return opportunityRepository.save(opportunity);
    }
    
    @Override
    public OpportunityEntity updateOpportunity(Long id, OpportunityEntity opportunity) {
        Optional<OpportunityEntity> existingOpportunity = opportunityRepository.findById(id);
        if (existingOpportunity.isPresent()) {
            OpportunityEntity updatedOpportunity = existingOpportunity.get();
            updatedOpportunity.setOpportunityCode(opportunity.getOpportunityCode());
            updatedOpportunity.setName(opportunity.getName());
            updatedOpportunity.setCustomerId(opportunity.getCustomerId());
            updatedOpportunity.setCustomerName(opportunity.getCustomerName());
            updatedOpportunity.setEstimatedAmount(opportunity.getEstimatedAmount());
            updatedOpportunity.setWinProbability(opportunity.getWinProbability());
            updatedOpportunity.setStage(opportunity.getStage());
            updatedOpportunity.setExpectedCloseDate(opportunity.getExpectedCloseDate());
            updatedOpportunity.setOwner(opportunity.getOwner());
            updatedOpportunity.setSource(opportunity.getSource());
            updatedOpportunity.setStatus(opportunity.getStatus());
            updatedOpportunity.setRemark(opportunity.getRemark());
            updatedOpportunity.setCreatedBy(opportunity.getCreatedBy());
            updatedOpportunity.setUpdatedBy(opportunity.getUpdatedBy());
            return opportunityRepository.save(updatedOpportunity);
        }
        return null;
    }
    
    @Override
    public void deleteOpportunity(Long id) {
        opportunityRepository.deleteById(id);
    }
    
    @Override
    public Optional<OpportunityEntity> getOpportunityById(Long id) {
        return opportunityRepository.findById(id);
    }
    
    @Override
    public OpportunityEntity getOpportunityByOpportunityCode(String opportunityCode) {
        return opportunityRepository.findByOpportunityCode(opportunityCode);
    }
    
    @Override
    public List<OpportunityEntity> getAllOpportunities() {
        return opportunityRepository.findAll();
    }
    
    @Override
    public PageResult<OpportunityEntity> getOpportunitiesByPage(Pageable pageable) {
        Page<OpportunityEntity> page = opportunityRepository.findAll(pageable);
        return PageResult.build(
            page.getTotalElements(),
            page.getSize(),
            page.getNumber() + 1,
            page.getContent()
        );
    }
    
    @Override
    public List<OpportunityEntity> getOpportunitiesByCustomerId(Long customerId) {
        return opportunityRepository.findByCustomerId(customerId);
    }
    
    @Override
    public List<OpportunityEntity> getOpportunitiesByOwner(String owner) {
        return opportunityRepository.findByOwner(owner);
    }
    
    @Override
    public List<OpportunityEntity> getOpportunitiesByStage(String stage) {
        return opportunityRepository.findByStage(stage);
    }
}