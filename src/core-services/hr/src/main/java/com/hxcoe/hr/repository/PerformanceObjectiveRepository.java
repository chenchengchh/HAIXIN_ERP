package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.PerformanceObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 绩效目标仓库接口
 */
@Repository
public interface PerformanceObjectiveRepository extends JpaRepository<PerformanceObjectiveEntity, Long> {

    /**
     * 根据员工ID查询绩效目标
     * @param employeeId 员工ID
     * @return 绩效目标列表
     */
    List<PerformanceObjectiveEntity> findByEmployee_Id(Long employeeId);
}
