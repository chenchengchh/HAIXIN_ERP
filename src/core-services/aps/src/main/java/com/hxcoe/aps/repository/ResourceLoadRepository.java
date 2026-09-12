package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ResourceLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceLoadRepository extends JpaRepository<ResourceLoadEntity, Long> {
    java.util.List<ResourceLoadEntity> findByPlanId(Long planId);
    
    java.util.List<ResourceLoadEntity> findByPlanIdAndResourceIdIn(Long planId, java.util.List<Long> resourceIds);
    
    java.util.List<ResourceLoadEntity> findByResourceIdIn(java.util.List<Long> resourceIds);
}