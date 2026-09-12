package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ScheduleHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleHistoryRepository extends JpaRepository<ScheduleHistoryEntity, Long> {

/**
* 根据计划ID查询调度历史
* @param planId 计划ID
* @return 调度历史列表;
*/
List<ScheduleHistoryEntity> findByPlanId(Long planId);
}

