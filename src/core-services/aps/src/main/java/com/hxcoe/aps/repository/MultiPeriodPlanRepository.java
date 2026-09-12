package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.MultiPeriodPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultiPeriodPlanRepository extends JpaRepository<MultiPeriodPlanEntity, Long> {
    MultiPeriodPlanEntity findByPlanNo(String planNo);

    java.util.List<MultiPeriodPlanEntity> findBySourcePlanId(Long sourcePlanId);
}
