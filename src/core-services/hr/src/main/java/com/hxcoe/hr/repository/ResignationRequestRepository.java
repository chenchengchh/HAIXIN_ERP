package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.ResignationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 离职申请Repository
 */
public interface ResignationRequestRepository extends JpaRepository<ResignationRequestEntity, Long>, JpaSpecificationExecutor<ResignationRequestEntity> {

    /**
     * 根据员工ID查询离职申请
     * @param employeeId 员工ID
     * @return 离职申请列表
     */
    List<ResignationRequestEntity> findByEmployeeId(Long employeeId);

    /**
     * 根据状态查询离职申请
     * @param status 状态
     * @return 离职申请列表
     */
    List<ResignationRequestEntity> findByStatus(Integer status);
}