package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.DepartmentEntity;
import com.hxcoe.hr.repository.DepartmentRepository;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 部门服务实现类
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 创建部门
     *
     * @param department 部门实体
     * @return 创建后的部门实体
     */
    @Override
    public DepartmentEntity createDepartment(DepartmentEntity department) {
        // 可以添加部门编号生成逻辑等
        return departmentRepository.save(department);
    }

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param department 部门实体
     * @return 更新后的部门实体
     */
    @Override
    public DepartmentEntity updateDepartment(Long id, DepartmentEntity department) {
        Optional<DepartmentEntity> existingDepartment = departmentRepository.findById(id);
        if (existingDepartment.isPresent()) {
            department.setId(id);
            // 可以添加更新前后的逻辑，如审计日志等
            return departmentRepository.save(department);
        }
        throw new RuntimeException("Department not found with id: " + id);
    }

    /**
     * 根据ID删除部门
     *
     * @param id 部门ID
     */
    @Override
    public void deleteDepartment(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Department not found with id: " + id);
        }
    }

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门实体
     */
    @Override
    public Optional<DepartmentEntity> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    /**
     * 根据部门编号查询部门
     *
     * @param departmentCode 部门编号
     * @return 部门实体
     */
    @Override
    public Optional<DepartmentEntity> getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode);
    }

    /**
     * 查询所有部门
     *
     * @return 部门列表
     */
    @Override
    public List<DepartmentEntity> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * 分页查询部门
     *
     * @param pageable 分页参数
     * @return 分页部门列表
     */
    @Override
    public Page<DepartmentEntity> getDepartmentsByPage(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    /**
     * 查询根部门列表
     *
     * @return 根部门列表
     */
    @Override
    public List<DepartmentEntity> getRootDepartments() {
        return departmentRepository.findByParentIsNull();
    }

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    @Override
    public List<DepartmentEntity> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentId(parentId);
    }

    /**
     * 根据部门状态查询部门列表
     *
     * @param status 部门状态
     * @return 部门列表
     */
    @Override
    public List<DepartmentEntity> getDepartmentsByStatus(String status) {
        return departmentRepository.findByStatus(status);
    }

    /**
     * 搜索部门
     *
     * @param keyword 搜索关键词
     * @return 部门列表
     */
    @Override
    public List<DepartmentEntity> searchDepartments(String keyword) {
        return departmentRepository.findByNameContaining(keyword);
    }

    /**
     * 查询部门及其所有子部门
     *
     * @param id 部门ID
     * @return 部门及其子部门列表
     */
    @Override
    public List<DepartmentEntity> getDepartmentWithChildren(Long id) {
        return departmentRepository.findDepartmentWithChildren(id);
    }

    /**
     * 设置部门负责人
     *
     * @param departmentId 部门ID
     * @param managerId 部门负责人ID
     * @return 更新后的部门实体
     */
    @Override
    public DepartmentEntity setDepartmentManager(Long departmentId, Long managerId) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        
        department.setManager(employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + managerId)));
        
        return departmentRepository.save(department);
    }

    /**
     * 批量删除部门
     *
     * @param ids 部门ID列表
     */
    @Override
    public void batchDeleteDepartments(List<Long> ids) {
        departmentRepository.deleteAllById(ids);
    }
}