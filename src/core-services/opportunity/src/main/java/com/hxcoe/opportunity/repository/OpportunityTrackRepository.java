package com.hxcoe.opportunity.repository;

import com.hxcoe.opportunity.entity.OpportunityTrackEntity;
import com.hxcoe.opportunity.entity.OpportunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityTrackRepository extends JpaRepository<OpportunityTrackEntity, Long>, JpaSpecificationExecutor<OpportunityTrackEntity> {
    java.util.List<OpportunityTrackEntity> findByOpportunity(OpportunityEntity opportunity);
    java.util.List<OpportunityTrackEntity> findByTrackType(String trackType);
    java.util.List<OpportunityTrackEntity> findByTracker(String tracker);
}