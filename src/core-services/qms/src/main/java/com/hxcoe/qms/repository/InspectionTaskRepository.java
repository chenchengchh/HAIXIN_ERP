package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.InspectionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 检验任务仓库
 */
public interface InspectionTaskRepository extends JpaRepository<InspectionTaskEntity, Long>, JpaSpecificationExecutor<InspectionTaskEntity> {

    /**
     * 根据任务编号查询
     *
     * @param taskNo 任务编号
     * @return 任务
     */
    Optional<InspectionTaskEntity> findByTaskNo(String taskNo);
}
