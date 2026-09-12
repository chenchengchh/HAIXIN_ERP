package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.ResignationRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 离职申请Service接口
 */
public interface ResignationRequestService {

    /**
     * 创建离职申请
     * @param resignationRequest 离职申请实体
     * @return 离职申请实体
     */
    ResignationRequestEntity createResignationRequest(ResignationRequestEntity resignationRequest);

    /**
     * 根据ID查询离职申请
     * @param id 离职申请ID
     * @return 离职申请实体
     */
    ResignationRequestEntity getResignationRequestById(Long id);

    /**
     * 更新离职申请
     * @param id 离职申请ID
     * @param resignationRequest 离职申请实体
     * @return 离职申请实体
     */
    ResignationRequestEntity updateResignationRequest(Long id, ResignationRequestEntity resignationRequest);

    /**
     * 删除离职申请
     * @param id 离职申请ID
     */
    void deleteResignationRequest(Long id);

    /**
     * 查询所有离职申请
     * @return 离职申请列表
     */
    List<ResignationRequestEntity> getAllResignationRequests();

    /**
     * 分页查询离职申请
     * @param pageable 分页参数
     * @return 离职申请分页列表
     */
    Page<ResignationRequestEntity> getResignationRequestsByPage(Pageable pageable);

    /**
     * 根据员工ID查询离职申请
     * @param employeeId 员工ID
     * @return 离职申请列表
     */
    List<ResignationRequestEntity> getResignationRequestsByEmployeeId(Long employeeId);

    /**
     * 根据状态查询离职申请
     * @param status 状态
     * @return 离职申请列表
     */
    List<ResignationRequestEntity> getResignationRequestsByStatus(Integer status);
}