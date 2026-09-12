package com.hxcoe.opportunity.repository;

import com.hxcoe.opportunity.entity.OpportunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityRepository extends JpaRepository<OpportunityEntity, Long>, JpaSpecificationExecutor<OpportunityEntity> {
    OpportunityEntity findByOpportunityCode(String opportunityCode);
    java.util.List<OpportunityEntity> findByCustomerId(Long customerId);
    java.util.List<OpportunityEntity> findByOwner(String owner);
    java.util.List<OpportunityEntity> findByStage(String stage);
    java.util.List<OpportunityEntity> findByStatus(String status);
}