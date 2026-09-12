package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 部门仓库接口
 */
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity> {

    /**
     * 根据部门名称查询部门
     *
     * @param name 部门名称
     * @return 部门信息
     */
    Optional<DepartmentEntity> findByName(String name);

    /**
     * 根据部门编号查询部门
     *
     * @param departmentCode 部门编号
     * @return 部门信息
     */
    Optional<DepartmentEntity> findByDepartmentCode(String departmentCode);

    /**
     * 查询根部门列表
     *
     * @return 根部门列表
     */
    List<DepartmentEntity> findByParentIsNull();

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<DepartmentEntity> findByParentId(Long parentId);

    /**
     * 根据部门状态查询部门列表
     *
     * @param status 部门状态
     * @return 部门列表
     */
    List<DepartmentEntity> findByStatus(String status);

    /**
     * 根据部门名称模糊查询
     *
     * @param name 部门名称
     * @return 部门列表
     */
    List<DepartmentEntity> findByNameContaining(String name);

    /**
     * 查询部门及其所有子部门
     *
     * @param id 部门ID
     * @return 部门及其子部门列表
     */
    @Query("SELECT d FROM DepartmentEntity d WHERE d.id = :id OR d.parent.id = :id OR d.parent.parent.id = :id")
    List<DepartmentEntity> findDepartmentWithChildren(@Param("id") Long id);

    /**
     * 查询部门负责人所在的部门
     *
     * @param managerId 部门负责人ID
     * @return 部门信息
     */
    Optional<DepartmentEntity> findByManagerId(Long managerId);
}