package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlanEntity, Long> {
    ProductionPlanEntity findByPlanNo(String planNo);
    
    ProductionPlanEntity findByPlanName(String planName);
    
    java.util.List<ProductionPlanEntity> findByStatus(String status);
}

