package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.PerformanceAppraisalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 绩效评估仓库接口
 */
@Repository
public interface PerformanceAppraisalRepository extends JpaRepository<PerformanceAppraisalEntity, Long> {

    /**
     * 根据员工ID查询绩效评估
     * @param employeeId 员工ID
     * @return 绩效评估列表
     */
    List<PerformanceAppraisalEntity> findByEmployee_Id(Long employeeId);
}
