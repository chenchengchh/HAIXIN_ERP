package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ScheduleHistoryEntity;
import com.hxcoe.aps.repository.ScheduleHistoryRepository;
import com.hxcoe.aps.service.ScheduleHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleHistoryServiceImpl implements ScheduleHistoryService {

    @Autowired
    private ScheduleHistoryRepository scheduleHistoryRepository;

    @Override
    public List<ScheduleHistoryEntity> getAllHistories() {
        return scheduleHistoryRepository.findAll();
    }

    @Override
    public ScheduleHistoryEntity createHistory(ScheduleHistoryEntity history) {
        return scheduleHistoryRepository.save(history);
    }

    @Override
    public ScheduleHistoryEntity getHistoryById(Long id) {
        return scheduleHistoryRepository.findById(id).orElse(null);
    }

    @Override
    public List<ScheduleHistoryEntity> getHistoriesByPlanId(Long planId) {
        return scheduleHistoryRepository.findByPlanId(planId);
    }

    @Override
    public List<ScheduleHistoryEntity> getHistoriesByScheduleId(Long scheduleId) {
        // 由于我们修改了实体类，scheduleId字段不再存在，我们可以返回空列表或根据实际需求调整
        return List.of();
    }

    @Override
    public void deleteHistory(Long id) {
        scheduleHistoryRepository.deleteById(id);
    }

    @Override
    public List<ScheduleHistoryEntity> batchCreateHistories(List<ScheduleHistoryEntity> histories) {
        return scheduleHistoryRepository.saveAll(histories);
    }
}
