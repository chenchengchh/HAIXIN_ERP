package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.TrainingParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 培训参与Repository
 */
public interface TrainingParticipantRepository extends JpaRepository<TrainingParticipantEntity, Long>, JpaSpecificationExecutor<TrainingParticipantEntity> {

    /**
     * 根据培训计划ID查询参与记录
     * @param trainingId 培训计划ID
     * @return 培训参与记录列表
     */
    List<TrainingParticipantEntity> findByTrainingId(Long trainingId);

    /**
     * 根据员工ID查询参与记录
     * @param employeeId 员工ID
     * @return 培训参与记录列表
     */
    List<TrainingParticipantEntity> findByEmployeeId(Long employeeId);
}
