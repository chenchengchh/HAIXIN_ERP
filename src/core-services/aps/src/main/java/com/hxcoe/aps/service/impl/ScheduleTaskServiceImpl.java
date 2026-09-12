package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.ScheduleTaskEntity;
import com.hxcoe.aps.repository.ScheduleTaskRepository;
import com.hxcoe.aps.service.ScheduleTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleTaskServiceImpl.class);

    @Autowired
    private ScheduleTaskRepository scheduleTaskRepository;

    @Override
    public ScheduleTaskEntity createTask(ScheduleTaskEntity task) {
        try {
            logger.info("创建排程任务");
            return scheduleTaskRepository.save(task);
        } catch (Exception e) {
            logger.error("创建排程任务失败", e);
            throw new RuntimeException("创建排程任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ScheduleTaskEntity getTaskById(Long id) {
        try {
            logger.info("查询排程任务: id={}", id);
            return scheduleTaskRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("查询排程任务失败", e);
            throw new RuntimeException("查询排程任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScheduleTaskEntity> getAllTasks() {
        try {
            logger.info("获取所有排程任务");
            return scheduleTaskRepository.findAll();
        } catch (Exception e) {
            logger.error("获取所有排程任务失败", e);
            throw new RuntimeException("获取所有排程任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ScheduleTaskEntity> getTasksByPage(Pageable pageable) {
        try {
            logger.info("分页获取排程任务: page={}, size={}", pageable.getPageNumber() + 1, pageable.getPageSize());
            return scheduleTaskRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("分页获取排程任务失败", e);
            throw new RuntimeException("分页获取排程任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ScheduleTaskEntity> getTasksByScheduleId(Long scheduleId, Pageable pageable) {
        try {
            logger.info("根据排程ID获取任务: scheduleId={}, page={}, size={}", scheduleId, pageable.getPageNumber() + 1, pageable.getPageSize());
            return scheduleTaskRepository.findByScheduleResultId(scheduleId, pageable);
        } catch (Exception e) {
            logger.error("根据排程ID获取任务失败", e);
            throw new RuntimeException("根据排程ID获取任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ScheduleTaskEntity> getTasksByResourceId(Long resourceId, Pageable pageable) {
        try {
            logger.info("根据资源ID获取任务: resourceId={}, page={}, size={}", resourceId, pageable.getPageNumber() + 1, pageable.getPageSize());
            return scheduleTaskRepository.findByResourceId(resourceId, pageable);
        } catch (Exception e) {
            logger.error("根据资源ID获取任务失败", e);
            throw new RuntimeException("根据资源ID获取任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ScheduleTaskEntity updateTask(Long id, ScheduleTaskEntity task) {
        try {
            logger.info("更新排程任务: id={}", id);
            task.setId(id);
            return scheduleTaskRepository.save(task);
        } catch (Exception e) {
            logger.error("更新排程任务失败", e);
            throw new RuntimeException("更新排程任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTask(Long id) {
        try {
            logger.info("删除排程任务: id={}", id);
            scheduleTaskRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("删除排程任务失败", e);
            throw new RuntimeException("删除排程任务失败: " + e.getMessage(), e);
        }
    }
}
