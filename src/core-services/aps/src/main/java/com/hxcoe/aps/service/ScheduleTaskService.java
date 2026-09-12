package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.ScheduleTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ScheduleTaskService {
    ScheduleTaskEntity createTask(ScheduleTaskEntity task);
    ScheduleTaskEntity getTaskById(Long id);
    List<ScheduleTaskEntity> getAllTasks();
    Page<ScheduleTaskEntity> getTasksByPage(Pageable pageable);
    Page<ScheduleTaskEntity> getTasksByScheduleId(Long scheduleId, Pageable pageable);
    Page<ScheduleTaskEntity> getTasksByResourceId(Long resourceId, Pageable pageable);
    ScheduleTaskEntity updateTask(Long id, ScheduleTaskEntity task);
    void deleteTask(Long id);
}
