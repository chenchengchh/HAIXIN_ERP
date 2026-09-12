package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.DepartmentEntity;
import com.hxcoe.oa.entity.UserEntity;
import com.hxcoe.oa.integration.HrIntegrationService;
import com.hxcoe.oa.repository.DepartmentRepository;
import com.hxcoe.oa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据同步服务
 * 用于将其他模块的数据同步到OA模块
 */
@Service
public class DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncService.class);

    @Autowired
    private HrIntegrationService hrIntegrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 同步HR部门数据到OA模块
     */
    @Transactional
    public void syncHrDepartments() {
        logger.info("开始同步HR部门数据...");
        try {
            List<DepartmentEntity> hrDepartments = hrIntegrationService.getAllDepartments();
            for (DepartmentEntity hrDept : hrDepartments) {
                // 检查部门是否已存在
                DepartmentEntity existingDept = departmentRepository.findByCode(hrDept.getCode());
                if (existingDept != null) {
                    // 更新现有部门
                    existingDept.setName(hrDept.getName());
                    existingDept.setDescription(hrDept.getDescription());
                    existingDept.setParentId(hrDept.getParentId());
                    existingDept.setParentName(hrDept.getParentName());
                    existingDept.setLevel(hrDept.getLevel());
                    existingDept.setStatus(hrDept.getStatus());
                    existingDept.setManagerId(hrDept.getManagerId());
                    existingDept.setManagerName(hrDept.getManagerName());
                    departmentRepository.save(existingDept);
                } else {
                    // 创建新部门
                    departmentRepository.save(hrDept);
                }
            }
            logger.info("HR部门数据同步完成");
        } catch (Exception e) {
            logger.error("同步HR部门数据失败", e);
            throw e;
        }
    }

    /**
     * 同步HR员工数据到OA模块
     */
    @Transactional
    public void syncHrEmployees() {
        logger.info("开始同步HR员工数据...");
        try {
            List<UserEntity> hrEmployees = hrIntegrationService.getAllEmployees();
            for (UserEntity hrEmp : hrEmployees) {
                // 检查用户是否已存在
                UserEntity existingUser = userRepository.findByUsername(hrEmp.getUsername());
                if (existingUser != null) {
                    // 更新现有用户
                    existingUser.setRealName(hrEmp.getRealName());
                    existingUser.setDepartmentId(hrEmp.getDepartmentId());
                    existingUser.setDepartmentName(hrEmp.getDepartmentName());
                    existingUser.setPosition(hrEmp.getPosition());
                    existingUser.setEmail(hrEmp.getEmail());
                    existingUser.setPhone(hrEmp.getPhone());
                    existingUser.setStatus(hrEmp.getStatus());
                    existingUser.setEmployeeId(hrEmp.getId());
                    userRepository.save(existingUser);
                } else {
                    // 创建新用户
                    hrEmp.setEmployeeId(hrEmp.getId());
                    userRepository.save(hrEmp);
                }
            }
            logger.info("HR员工数据同步完成");
        } catch (Exception e) {
            logger.error("同步HR员工数据失败", e);
            throw e;
        }
    }

    /**
     * 同步所有HR数据到OA模块
     */
    @Transactional
    public void syncAllHrData() {
        logger.info("开始同步所有HR数据...");
        try {
            // 先同步部门数据，因为员工数据依赖部门数据
            syncHrDepartments();
            syncHrEmployees();
            logger.info("所有HR数据同步完成");
        } catch (Exception e) {
            logger.error("同步所有HR数据失败", e);
            throw e;
        }
    }

    /**
     * 根据部门ID同步该部门的员工数据
     * @param departmentId 部门ID
     */
    @Transactional
    public void syncEmployeesByDepartment(Long departmentId) {
        logger.info("开始同步部门ID为{}的员工数据...", departmentId);
        try {
            List<UserEntity> deptEmployees = hrIntegrationService.getEmployeesByDepartmentId(departmentId);
            for (UserEntity emp : deptEmployees) {
                // 检查用户是否已存在
                UserEntity existingUser = userRepository.findByUsername(emp.getUsername());
                if (existingUser != null) {
                    // 更新现有用户
                    existingUser.setRealName(emp.getRealName());
                    existingUser.setDepartmentId(emp.getDepartmentId());
                    existingUser.setDepartmentName(emp.getDepartmentName());
                    existingUser.setPosition(emp.getPosition());
                    existingUser.setEmail(emp.getEmail());
                    existingUser.setPhone(emp.getPhone());
                    existingUser.setStatus(emp.getStatus());
                    existingUser.setEmployeeId(emp.getId());
                    userRepository.save(existingUser);
                } else {
                    // 创建新用户
                    emp.setEmployeeId(emp.getId());
                    userRepository.save(emp);
                }
            }
            logger.info("部门ID为{}的员工数据同步完成", departmentId);
        } catch (Exception e) {
            logger.error("同步部门ID为{}的员工数据失败", departmentId, e);
            throw e;
        }
    }
}
