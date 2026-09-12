package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.MultiPeriodPlanItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultiPeriodPlanItemRepository extends JpaRepository<MultiPeriodPlanItemEntity, Long> {
    java.util.List<MultiPeriodPlanItemEntity> findByPlanIdOrderByPeriodIndexAsc(Long planId);

    void deleteByPlanId(Long planId);
}
