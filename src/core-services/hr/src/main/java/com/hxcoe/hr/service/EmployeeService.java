package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 创建员工
     *
     * @param employee 员工实体
     * @return 创建后的员工实体
     */
    EmployeeEntity createEmployee(EmployeeEntity employee);

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param employee 员工实体
     * @return 更新后的员工实体
     */
    EmployeeEntity updateEmployee(Long id, EmployeeEntity employee);

    /**
     * 根据ID删除员工
     *
     * @param id 员工ID
     */
    void deleteEmployee(Long id);

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 员工实体
     */
    Optional<EmployeeEntity> getEmployeeById(Long id);

    /**
     * 根据员工编号查询员工
     *
     * @param employeeCode 员工编号
     * @return 员工实体
     */
    Optional<EmployeeEntity> getEmployeeByCode(String employeeCode);

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    List<EmployeeEntity> getAllEmployees();

    /**
     * 分页查询员工
     *
     * @param pageable 分页参数
     * @return 分页员工列表
     */
    Page<EmployeeEntity> getEmployeesByPage(Pageable pageable);

    /**
     * 根据部门ID查询员工
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    List<EmployeeEntity> getEmployeesByDepartment(Long departmentId);

    /**
     * 根据岗位ID查询员工
     *
     * @param positionId 岗位ID
     * @return 员工列表
     */
    List<EmployeeEntity> getEmployeesByPosition(Long positionId);

    /**
     * 根据员工状态查询员工
     *
     * @param status 员工状态
     * @return 员工列表
     */
    List<EmployeeEntity> getEmployeesByStatus(String status);

    /**
     * 搜索员工
     *
     * @param keyword 搜索关键词
     * @return 员工列表
     */
    List<EmployeeEntity> searchEmployees(String keyword);

    /**
     * 查询部门负责人
     *
     * @param departmentId 部门ID
     * @return 部门负责人
     */
    Optional<EmployeeEntity> getDepartmentManager(Long departmentId);

    /**
     * 批量导入员工
     *
     * @param employees 员工列表
     * @return 导入结果
     */
    List<EmployeeEntity> batchImportEmployees(List<EmployeeEntity> employees);

    /**
     * 批量删除员工
     *
     * @param ids 员工ID列表
     */
    void batchDeleteEmployees(List<Long> ids);
}