package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.DepartmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 创建部门
     *
     * @param department 部门实体
     * @return 创建后的部门实体
     */
    DepartmentEntity createDepartment(DepartmentEntity department);

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param department 部门实体
     * @return 更新后的部门实体
     */
    DepartmentEntity updateDepartment(Long id, DepartmentEntity department);

    /**
     * 根据ID删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(Long id);

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门实体
     */
    Optional<DepartmentEntity> getDepartmentById(Long id);

    /**
     * 根据部门编号查询部门
     *
     * @param departmentCode 部门编号
     * @return 部门实体
     */
    Optional<DepartmentEntity> getDepartmentByCode(String departmentCode);

    /**
     * 查询所有部门
     *
     * @return 部门列表
     */
    List<DepartmentEntity> getAllDepartments();

    /**
     * 分页查询部门
     *
     * @param pageable 分页参数
     * @return 分页部门列表
     */
    Page<DepartmentEntity> getDepartmentsByPage(Pageable pageable);

    /**
     * 查询根部门列表
     *
     * @return 根部门列表
     */
    List<DepartmentEntity> getRootDepartments();

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<DepartmentEntity> getChildDepartments(Long parentId);

    /**
     * 根据部门状态查询部门列表
     *
     * @param status 部门状态
     * @return 部门列表
     */
    List<DepartmentEntity> getDepartmentsByStatus(String status);

    /**
     * 搜索部门
     *
     * @param keyword 搜索关键词
     * @return 部门列表
     */
    List<DepartmentEntity> searchDepartments(String keyword);

    /**
     * 查询部门及其所有子部门
     *
     * @param id 部门ID
     * @return 部门及其子部门列表
     */
    List<DepartmentEntity> getDepartmentWithChildren(Long id);

    /**
     * 设置部门负责人
     *
     * @param departmentId 部门ID
     * @param managerId 部门负责人ID
     * @return 更新后的部门实体
     */
    DepartmentEntity setDepartmentManager(Long departmentId, Long managerId);

    /**
     * 批量删除部门
     *
     * @param ids 部门ID列表
     */
    void batchDeleteDepartments(List<Long> ids);
}