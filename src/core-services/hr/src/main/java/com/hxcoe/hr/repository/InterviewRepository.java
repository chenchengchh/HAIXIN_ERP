package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.InterviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 面试仓库接口
 */
@Repository
public interface InterviewRepository extends JpaRepository<InterviewEntity, Long> {
}