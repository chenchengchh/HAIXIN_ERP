package com.hxcoe.scrm.repository.douyin;

import com.hxcoe.scrm.entity.douyin.TaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLogEntity, Long> {
    List<TaskLogEntity> findByTaskIdOrderByCreateTimeAsc(Long taskId);
}
