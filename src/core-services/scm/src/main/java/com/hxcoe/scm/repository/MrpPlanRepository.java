package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.MrpPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MrpPlanRepository extends JpaRepository<MrpPlanEntity, Long> {
}
