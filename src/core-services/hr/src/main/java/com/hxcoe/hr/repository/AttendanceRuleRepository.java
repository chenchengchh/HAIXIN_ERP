package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.AttendanceRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 考勤规则Repository
 */
public interface AttendanceRuleRepository extends JpaRepository<AttendanceRuleEntity, Long>, JpaSpecificationExecutor<AttendanceRuleEntity> {

    /**
     * 根据状态查询考勤规则
     * @param status 状态（0禁用 1启用）
     * @return 考勤规则列表
     */
    List<AttendanceRuleEntity> findByStatus(Integer status);
}
