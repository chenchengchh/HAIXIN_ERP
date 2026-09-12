package com.hxcoe.aps.repository;

import com.hxcoe.aps.entity.ScheduleDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleDetailRepository extends JpaRepository<ScheduleDetailEntity, Long> {
    List<ScheduleDetailEntity> findByPlanId(Long planId);

    /**
     * 按排程结果查询详情：甘特图、下达MES、冲突检测均按单次排程结果取数
     */
    List<ScheduleDetailEntity> findByScheduleResultId(Long scheduleResultId);

    /**
     * 删除某计划下指定状态的详情：重排前清理未下达的旧详情，保留已下达(RELEASED)历史
     */
    void deleteByPlanIdAndStatus(Long planId, String status);
}
