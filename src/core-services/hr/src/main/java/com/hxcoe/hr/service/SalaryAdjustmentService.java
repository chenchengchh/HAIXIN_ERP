package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.SalaryAdjustmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 调薪记录Service接口
 */
public interface SalaryAdjustmentService {

    /**
     * 创建调薪记录
     * @param salaryAdjustment 调薪记录实体
     * @return 调薪记录实体
     */
    SalaryAdjustmentEntity createSalaryAdjustment(SalaryAdjustmentEntity salaryAdjustment);

    /**
     * 根据ID查询调薪记录
     * @param id 调薪记录ID
     * @return 调薪记录实体
     */
    SalaryAdjustmentEntity getSalaryAdjustmentById(Long id);

    /**
     * 更新调薪记录
     * @param id 调薪记录ID
     * @param salaryAdjustment 调薪记录实体
     * @return 调薪记录实体
     */
    SalaryAdjustmentEntity updateSalaryAdjustment(Long id, SalaryAdjustmentEntity salaryAdjustment);

    /**
     * 删除调薪记录
     * @param id 调薪记录ID
     */
    void deleteSalaryAdjustment(Long id);

    /**
     * 查询所有调薪记录
     * @return 调薪记录列表
     */
    List<SalaryAdjustmentEntity> getAllSalaryAdjustments();

    /**
     * 分页查询调薪记录
     * @param pageable 分页参数
     * @return 调薪记录分页列表
     */
    Page<SalaryAdjustmentEntity> getSalaryAdjustmentsByPage(Pageable pageable);

    /**
     * 根据员工ID查询调薪记录
     * @param employeeId 员工ID
     * @return 调薪记录列表
     */
    List<SalaryAdjustmentEntity> getSalaryAdjustmentsByEmployeeId(Long employeeId);

    /**
     * 根据状态查询调薪记录
     * @param status 状态
     * @return 调薪记录列表
     */
    List<SalaryAdjustmentEntity> getSalaryAdjustmentsByStatus(Integer status);
}