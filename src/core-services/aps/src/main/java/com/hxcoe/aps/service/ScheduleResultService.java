package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ScheduleResultService {

    /**
    * 创建调度结果
    * @param scheduleResult 调度结果实体
    * @return 创建后的调度结果实体;
    */
    ScheduleResultEntity createScheduleResult(ScheduleResultEntity scheduleResult);

    /**
    * 更新调度结果
    * @param id 调度结果ID
    * @param scheduleResult 更新的调度结果实体
    * @return 更新后的调度结果实体;
    */
    ScheduleResultEntity updateScheduleResult(Long id, ScheduleResultEntity scheduleResult);

    /**
    * 删除调度结果
    * @param id 调度结果ID
    */
    void deleteScheduleResult(Long id);

    /**
    * 根据ID查询调度结果
    * @param id 调度结果ID
    * @return 调度结果实体;
    */
    ScheduleResultEntity getScheduleResultById(Long id);

    /**
    * 查询所有调度结果
    * @return 调度结果列表;
    */
    List<ScheduleResultEntity> getAllScheduleResults();

    /**
    * 分页查询调度结果
    * @param pageable 分页参数
    * @return 调度结果分页列表;
    */
    Page<ScheduleResultEntity> getScheduleResultsByPage(Pageable pageable);
    
    /**
    * 分页查询调度结果（带条件）
    * @param pageable 分页参数
    * @param scheduleNo 排程编号
    * @param algorithm 使用算法
    * @return 调度结果分页列表;
    */
    Page<ScheduleResultEntity> getScheduleResultsByPage(Pageable pageable, String scheduleNo, String algorithm);

    /**
    * 根据生产计划ID查询调度结果
    * @param planId 生产计划ID
    * @return 调度结果列表;
    */
    List<ScheduleResultEntity> getScheduleResultsByPlanId(Long planId);

    /**
    * 根据调度状态查询调度结果
    * @param status 调度状态
    * @return 调度结果列表;
    */
    List<ScheduleResultEntity> getScheduleResultsByStatus(String status);
}

