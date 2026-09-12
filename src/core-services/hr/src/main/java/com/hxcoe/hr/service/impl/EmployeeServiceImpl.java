package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.service.EmployeeService;
import com.hxcoe.hr.service.HrEventOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 员工服务实现类
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HrEventOutboxService hrEventOutboxService;

    /**
     * 创建员工
     *
     * @param employee 员工实体
     * @return 创建后的员工实体
     */
    @Override
    public EmployeeEntity createEmployee(EmployeeEntity employee) {
        EmployeeEntity saved = employeeRepository.save(employee);
        // P2-C: 员工创建即入职，同事务入队 OA/ERP 员工入职事件
        hrEventOutboxService.enqueueEmployeeOnboarded(saved);
        return saved;
    }

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param employee 员工实体
     * @return 更新后的员工实体
     */
    @Override
    public EmployeeEntity updateEmployee(Long id, EmployeeEntity employee) {
        Optional<EmployeeEntity> existingEmployee = employeeRepository.findById(id);
        if (existingEmployee.isPresent()) {
            employee.setId(id);
            // 可以添加更新前后的逻辑，如审计日志等
            return employeeRepository.save(employee);
        }
        throw new RuntimeException("Employee not found with id: " + id);
    }

    /**
     * 根据ID删除员工
     *
     * @param id 员工ID
     */
    @Override
    public void deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Employee not found with id: " + id);
        }
    }

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 员工实体
     */
    @Override
    public Optional<EmployeeEntity> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * 根据员工编号查询员工
     *
     * @param employeeCode 员工编号
     * @return 员工实体
     */
    @Override
    public Optional<EmployeeEntity> getEmployeeByCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode);
    }

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    @Override
    public List<EmployeeEntity> getAllEmployees() {
        return employeeRepository.findAllWithDepartmentAndPosition();
    }

    /**
     * 分页查询员工
     *
     * @param pageable 分页参数
     * @return 分页员工列表
     */
    @Override
    public Page<EmployeeEntity> getEmployeesByPage(Pageable pageable) {
        return employeeRepository.findAllWithDepartmentAndPosition(pageable);
    }

    /**
     * 根据部门ID查询员工
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    @Override
    public List<EmployeeEntity> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdWithPosition(departmentId);
    }

    /**
     * 根据岗位ID查询员工
     *
     * @param positionId 岗位ID
     * @return 员工列表
     */
    @Override
    public List<EmployeeEntity> getEmployeesByPosition(Long positionId) {
        return employeeRepository.findByPositionIdWithDepartment(positionId);
    }

    /**
     * 根据员工状态查询员工
     *
     * @param status 员工状态
     * @return 员工列表
     */
    @Override
    public List<EmployeeEntity> getEmployeesByStatus(String status) {
        return employeeRepository.findByStatus(status);
    }

    /**
     * 搜索员工
     *
     * @param keyword 搜索关键词
     * @return 员工列表
     */
    @Override
    public List<EmployeeEntity> searchEmployees(String keyword) {
        return employeeRepository.findByNameContaining(keyword);
    }

    /**
     * 查询部门负责人
     *
     * @param departmentId 部门ID
     * @return 部门负责人
     */
    @Override
    public Optional<EmployeeEntity> getDepartmentManager(Long departmentId) {
        return employeeRepository.findDepartmentManager(departmentId);
    }

    /**
     * 批量导入员工
     *
     * @param employees 员工列表
     * @return 导入结果
     */
    @Override
    public List<EmployeeEntity> batchImportEmployees(List<EmployeeEntity> employees) {
        return employeeRepository.saveAll(employees);
    }

    /**
     * 批量删除员工
     *
     * @param ids 员工ID列表
     */
    @Override
    public void batchDeleteEmployees(List<Long> ids) {
        employeeRepository.deleteAllById(ids);
    }
}