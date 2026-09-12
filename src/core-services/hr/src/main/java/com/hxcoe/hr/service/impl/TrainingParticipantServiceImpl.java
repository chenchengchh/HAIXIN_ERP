package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.TrainingParticipantEntity;
import com.hxcoe.hr.repository.TrainingParticipantRepository;
import com.hxcoe.hr.service.TrainingParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 培训参与Service实现类
 */
@Service
public class TrainingParticipantServiceImpl implements TrainingParticipantService {

    @Autowired
    private TrainingParticipantRepository trainingParticipantRepository;

    /**
     * 创建培训参与记录
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    @Override
    public TrainingParticipantEntity createTrainingParticipant(TrainingParticipantEntity trainingParticipant) {
        return trainingParticipantRepository.save(trainingParticipant);
    }

    /**
     * 根据ID查询培训参与记录
     * @param id 培训参与记录ID
     * @return 培训参与实体
     */
    @Override
    public TrainingParticipantEntity getTrainingParticipantById(Long id) {
        return trainingParticipantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: id=" + id));
    }

    /**
     * 更新培训参与记录
     * @param id 培训参与记录ID
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    @Override
    public TrainingParticipantEntity updateTrainingParticipant(Long id, TrainingParticipantEntity trainingParticipant) {
        TrainingParticipantEntity existingParticipant = getTrainingParticipantById(id);
        // 更新培训参与记录字段
        existingParticipant.setTrainingId(trainingParticipant.getTrainingId());
        existingParticipant.setEmployeeId(trainingParticipant.getEmployeeId());
        existingParticipant.setAttendanceStatus(trainingParticipant.getAttendanceStatus());
        existingParticipant.setCompletionStatus(trainingParticipant.getCompletionStatus());
        existingParticipant.setScore(trainingParticipant.getScore());
        existingParticipant.setRemark(trainingParticipant.getRemark());
        return trainingParticipantRepository.save(existingParticipant);
    }

    /**
     * 删除培训参与记录
     * @param id 培训参与记录ID
     */
    @Override
    public void deleteTrainingParticipant(Long id) {
        TrainingParticipantEntity existingParticipant = getTrainingParticipantById(id);
        trainingParticipantRepository.delete(existingParticipant);
    }

    /**
     * 查询全部培训参与记录
     * @return 培训参与记录列表
     */
    @Override
    public List<TrainingParticipantEntity> getAllTrainingParticipants() {
        return trainingParticipantRepository.findAll();
    }

    /**
     * 分页查询培训参与记录
     * @param pageable 分页参数
     * @return 培训参与记录分页列表
     */
    @Override
    public Page<TrainingParticipantEntity> getTrainingParticipantsByPage(Pageable pageable) {
        return trainingParticipantRepository.findAll(pageable);
    }

    /**
     * 根据培训计划ID查询参与记录
     * @param trainingId 培训计划ID
     * @return 培训参与记录列表
     */
    @Override
    public List<TrainingParticipantEntity> getByTrainingId(Long trainingId) {
        return trainingParticipantRepository.findByTrainingId(trainingId);
    }

    /**
     * 根据员工ID查询参与记录
     * @param employeeId 员工ID
     * @return 培训参与记录列表
     */
    @Override
    public List<TrainingParticipantEntity> getByEmployeeId(Long employeeId) {
        return trainingParticipantRepository.findByEmployeeId(employeeId);
    }
}
