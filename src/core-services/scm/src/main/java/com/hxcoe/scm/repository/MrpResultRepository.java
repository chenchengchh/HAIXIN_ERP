package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.MrpResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MrpResultRepository extends JpaRepository<MrpResultEntity, Long>, JpaSpecificationExecutor<MrpResultEntity> {
    List<MrpResultEntity> findByPlanId(Long planId);
}
