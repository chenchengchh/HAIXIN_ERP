package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    /**
     * 根据父部门ID查询子部门列表
     */
    List<DepartmentEntity> findByParentId(Long parentId);

    /**
     * 根据部门状态查询
     */
    List<DepartmentEntity> findByStatus(Integer status);

    /**
     * 根据部门名称查询
     */
    DepartmentEntity findByName(String name);

    /**
     * 根据部门编码查询
     */
    DepartmentEntity findByCode(String code);

    /**
     * 根据部门名称模糊查询
     */
    List<DepartmentEntity> findByNameContaining(String name);
}