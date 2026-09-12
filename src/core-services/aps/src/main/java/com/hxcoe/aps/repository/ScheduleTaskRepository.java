package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ScheduleTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleTaskRepository extends JpaRepository<ScheduleTaskEntity, Long> {

/**
* 根据排程结果ID查询排程任务
* @param scheduleResultId 排程结果ID
* @return 排程任务列表;
*/
List<ScheduleTaskEntity> findByScheduleResultId(Long scheduleResultId);

/**
* 根据排程结果ID分页查询排程任务
* @param scheduleResultId 排程结果ID
* @param pageable 分页参数
* @return 分页排程任务列表;
*/
Page<ScheduleTaskEntity> findByScheduleResultId(Long scheduleResultId, Pageable pageable);

/**
* 鏍规嵁璧勬簮ID鏌ヨ鎺掔浠诲姟
* @param resourceId 璧勬簮ID
* @return 鎺掔浠诲姟鍒楄〃;
*/
List<ScheduleTaskEntity> findByResourceId(Long resourceId);

/**
* 根据资源ID分页查询排程任务
* @param resourceId 资源ID
* @param pageable 分页参数
* @return 分页排程任务列表;
*/
Page<ScheduleTaskEntity> findByResourceId(Long resourceId, Pageable pageable);
}

