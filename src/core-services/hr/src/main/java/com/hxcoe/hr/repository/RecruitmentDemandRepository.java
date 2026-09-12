package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.RecruitmentDemandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 招聘需求仓库接口
 */
@Repository
public interface RecruitmentDemandRepository extends JpaRepository<RecruitmentDemandEntity, Long> {
}