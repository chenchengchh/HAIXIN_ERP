package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 简历仓库接口
 */
@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
}