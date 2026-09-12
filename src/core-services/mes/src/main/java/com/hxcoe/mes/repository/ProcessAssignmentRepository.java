package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.ProcessAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessAssignmentRepository extends JpaRepository<ProcessAssignmentEntity, Long>, JpaSpecificationExecutor<ProcessAssignmentEntity> {
    List<ProcessAssignmentEntity> findByWorkOrderNoOrderByCreateTimeDesc(String workOrderNo);
}
