package com.hxcoe.oa.integration;

import com.hxcoe.oa.entity.DepartmentEntity;
import com.hxcoe.oa.entity.UserEntity;
import java.util.List;
import java.util.Optional;

/**
 * HR模块集成服务接口
 * 用于OA模块与HR模块之间的数据交互
 */
public interface HrIntegrationService {

    /**
     * 根据员工ID获取员工信息
     * @param employeeId 员工ID
     * @return 用户实体，包含员工信息
     */
    Optional<UserEntity> getEmployeeById(Long employeeId);

    /**
     * 根据员工编号获取员工信息
     * @param employeeNo 员工编号
     * @return 用户实体，包含员工信息
     */
    Optional<UserEntity> getEmployeeByNo(String employeeNo);

    /**
     * 获取所有员工信息
     * @return 员工列表
     */
    List<UserEntity> getAllEmployees();

    /**
     * 根据部门ID获取部门信息
     * @param departmentId 部门ID
     * @return 部门实体
     */
    Optional<DepartmentEntity> getDepartmentById(Long departmentId);

    /**
     * 根据部门编码获取部门信息
     * @param departmentCode 部门编码
     * @return 部门实体
     */
    Optional<DepartmentEntity> getDepartmentByCode(String departmentCode);

    /**
     * 获取所有部门信息
     * @return 部门列表
     */
    List<DepartmentEntity> getAllDepartments();

    /**
     * 根据部门ID获取部门员工列表
     * @param departmentId 部门ID
     * @return 部门员工列表
     */
    List<UserEntity> getEmployeesByDepartmentId(Long departmentId);
}
