package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.repository.ScheduleResultRepository;
import com.hxcoe.aps.service.ScheduleResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ScheduleResultServiceImpl implements ScheduleResultService {

    @Autowired
    private ScheduleResultRepository scheduleResultRepository;

    // 构造方法
    public ScheduleResultServiceImpl() {
        // 默认构造方法
    }

    @Override
    public ScheduleResultEntity createScheduleResult(ScheduleResultEntity scheduleResult) {
        return scheduleResultRepository.save(scheduleResult);
    }

    @Override
    public ScheduleResultEntity getScheduleResultById(Long id) {
        return scheduleResultRepository.findById(id).orElse(null);
    }

    @Override
    public List<ScheduleResultEntity> getAllScheduleResults() {
        return scheduleResultRepository.findAll();
    }

    @Override
    public Page<ScheduleResultEntity> getScheduleResultsByPage(Pageable pageable) {
        return scheduleResultRepository.findAll(pageable);
    }
    
    @Override
    public Page<ScheduleResultEntity> getScheduleResultsByPage(Pageable pageable, String scheduleNo, String algorithm) {
        if (scheduleNo != null && !scheduleNo.isEmpty() && algorithm != null && !algorithm.isEmpty()) {
            return scheduleResultRepository.findByScheduleNoAndAlgorithm(scheduleNo, algorithm, pageable);
        } else if (scheduleNo != null && !scheduleNo.isEmpty()) {
            return scheduleResultRepository.findByScheduleNo(scheduleNo, pageable);
        } else if (algorithm != null && !algorithm.isEmpty()) {
            return scheduleResultRepository.findByAlgorithm(algorithm, pageable);
        } else {
            return scheduleResultRepository.findAll(pageable);
        }
    }

    @Override
    public List<ScheduleResultEntity> getScheduleResultsByPlanId(Long planId) {
        return scheduleResultRepository.findByPlanId(planId);
    }

    @Override
    public List<ScheduleResultEntity> getScheduleResultsByStatus(String status) {
        return scheduleResultRepository.findByStatus(status);
    }

    @Override
    public ScheduleResultEntity updateScheduleResult(Long id, ScheduleResultEntity scheduleResult) {
        scheduleResult.setId(id);
        return scheduleResultRepository.save(scheduleResult);
    }

    @Override
    public void deleteScheduleResult(Long id) {
        scheduleResultRepository.deleteById(id);
    }
}
