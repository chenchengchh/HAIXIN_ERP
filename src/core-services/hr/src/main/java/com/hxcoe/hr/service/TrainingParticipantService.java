package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.TrainingParticipantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 培训参与Service接口
 */
public interface TrainingParticipantService {

    /**
     * 创建培训参与记录
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    TrainingParticipantEntity createTrainingParticipant(TrainingParticipantEntity trainingParticipant);

    /**
     * 根据ID查询培训参与记录
     * @param id 培训参与记录ID
     * @return 培训参与实体
     */
    TrainingParticipantEntity getTrainingParticipantById(Long id);

    /**
     * 更新培训参与记录
     * @param id 培训参与记录ID
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    TrainingParticipantEntity updateTrainingParticipant(Long id, TrainingParticipantEntity trainingParticipant);

    /**
     * 删除培训参与记录
     * @param id 培训参与记录ID
     */
    void deleteTrainingParticipant(Long id);

    /**
     * 查询全部培训参与记录
     * @return 培训参与记录列表
     */
    List<TrainingParticipantEntity> getAllTrainingParticipants();

    /**
     * 分页查询培训参与记录
     * @param pageable 分页参数
     * @return 培训参与记录分页列表
     */
    Page<TrainingParticipantEntity> getTrainingParticipantsByPage(Pageable pageable);

    /**
     * 根据培训计划ID查询参与记录
     * @param trainingId 培训计划ID
     * @return 培训参与记录列表
     */
    List<TrainingParticipantEntity> getByTrainingId(Long trainingId);

    /**
     * 根据员工ID查询参与记录
     * @param employeeId 员工ID
     * @return 培训参与记录列表
     */
    List<TrainingParticipantEntity> getByEmployeeId(Long employeeId);
}
