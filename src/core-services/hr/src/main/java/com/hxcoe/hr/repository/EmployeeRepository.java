package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 员工仓库接口
 */
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {

    /**
     * 根据员工编号查询员工
     *
     * @param employeeCode 员工编号
     * @return 员工信息
     */
    Optional<EmployeeEntity> findByEmployeeCode(String employeeCode);

    /**
     * 根据身份证号查询员工
     *
     * @param idCard 身份证号
     * @return 员工信息
     */
    Optional<EmployeeEntity> findByIdCard(String idCard);

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    List<EmployeeEntity> findByDepartmentId(Long departmentId);

    /**
     * 根据岗位ID查询员工列表
     *
     * @param positionId 岗位ID
     * @return 员工列表
     */
    List<EmployeeEntity> findByPositionId(Long positionId);

    /**
     * 根据员工状态查询员工列表
     *
     * @param status 员工状态
     * @return 员工列表
     */
    List<EmployeeEntity> findByStatus(String status);

    /**
     * 根据员工姓名模糊查询
     *
     * @param name 员工姓名
     * @return 员工列表
     */
    List<EmployeeEntity> findByNameContaining(String name);

    /**
     * 根据部门ID和员工状态查询员工列表
     *
     * @param departmentId 部门ID
     * @param status 员工状态
     * @return 员工列表
     */
    List<EmployeeEntity> findByDepartmentIdAndStatus(Long departmentId, String status);

    /**
     * 查询部门负责人
     *
     * @param departmentId 部门ID
     * @return 部门负责人
     */
    @Query("SELECT e FROM EmployeeEntity e JOIN DepartmentEntity d ON e.id = d.manager.id WHERE d.id = :departmentId")
    Optional<EmployeeEntity> findDepartmentManager(@Param("departmentId") Long departmentId);
    
    /**
     * 优化查询所有员工，只查询必要字段
     *
     * @return 员工列表
     */
    @Query("SELECT e FROM EmployeeEntity e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.position")
    List<EmployeeEntity> findAllWithDepartmentAndPosition();
    
    /**
     * 优化分页查询员工，只查询必要字段
     *
     * @return 员工分页列表
     */
    @Query("SELECT e FROM EmployeeEntity e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.position")
    org.springframework.data.domain.Page<EmployeeEntity> findAllWithDepartmentAndPosition(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 优化根据部门ID查询员工，只查询必要字段
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    @Query("SELECT e FROM EmployeeEntity e LEFT JOIN FETCH e.position WHERE e.department.id = :departmentId")
    List<EmployeeEntity> findByDepartmentIdWithPosition(@Param("departmentId") Long departmentId);
    
    /**
     * 优化根据岗位ID查询员工，只查询必要字段
     *
     * @param positionId 岗位ID
     * @return 员工列表
     */
    @Query("SELECT e FROM EmployeeEntity e LEFT JOIN FETCH e.department WHERE e.position.id = :positionId")
    List<EmployeeEntity> findByPositionIdWithDepartment(@Param("positionId") Long positionId);
}