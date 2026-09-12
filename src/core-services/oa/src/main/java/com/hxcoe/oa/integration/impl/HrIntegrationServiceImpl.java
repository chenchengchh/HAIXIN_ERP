package com.hxcoe.oa.integration.impl;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.DepartmentEntity;
import com.hxcoe.oa.entity.UserEntity;
import com.hxcoe.oa.integration.HrIntegrationService;
import com.hxcoe.oa.integration.client.HrDepartmentFeignClient;
import com.hxcoe.oa.integration.client.HrEmployeeFeignClient;
import com.hxcoe.oa.integration.dto.HrDepartmentDTO;
import com.hxcoe.oa.integration.dto.HrEmployeeDTO;
import com.hxcoe.oa.web.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * HR模块集成服务实现类
 * 通过Feign调用HR模块获取数据
 */
@Service
public class HrIntegrationServiceImpl implements HrIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(HrIntegrationServiceImpl.class);

    @Autowired
    private HrEmployeeFeignClient hrEmployeeFeignClient;

    @Autowired
    private HrDepartmentFeignClient hrDepartmentFeignClient;

    /**
     * 根据员工ID获取员工信息
     * @param employeeId 员工ID
     * @return 用户实体，包含员工信息
     */
    @Override
    public Optional<UserEntity> getEmployeeById(Long employeeId) {
        try {
            HrEmployeeDTO employee = unwrap(
                    hrEmployeeFeignClient.getById(employeeId, currentTraceId()),
                    "getEmployeeById",
                    employeeId
            );
            return Optional.ofNullable(toUserEntity(employee));
        } catch (Exception e) {
            logger.error("Failed to get employee by id: {}", employeeId, e);
            return Optional.empty();
        }
    }

    /**
     * 根据员工编号获取员工信息
     * @param employeeNo 员工编号
     * @return 用户实体，包含员工信息
     */
    @Override
    public Optional<UserEntity> getEmployeeByNo(String employeeNo) {
        try {
            HrEmployeeDTO employee = unwrap(
                    hrEmployeeFeignClient.getByCode(employeeNo, currentTraceId()),
                    "getEmployeeByNo",
                    employeeNo
            );
            return Optional.ofNullable(toUserEntity(employee));
        } catch (Exception e) {
            logger.error("Failed to get employee by no: {}", employeeNo, e);
            return Optional.empty();
        }
    }

    /**
     * 获取所有员工信息
     * @return 员工列表
     */
    @Override
    public List<UserEntity> getAllEmployees() {
        try {
            List<HrEmployeeDTO> employees = unwrap(
                    hrEmployeeFeignClient.listAll(currentTraceId()),
                    "getAllEmployees",
                    null
            );
            return toUserEntities(employees);
        } catch (Exception e) {
            logger.error("Failed to get all employees", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据部门ID获取部门信息
     * @param departmentId 部门ID
     * @return 部门实体
     */
    @Override
    public Optional<DepartmentEntity> getDepartmentById(Long departmentId) {
        try {
            HrDepartmentDTO department = unwrap(
                    hrDepartmentFeignClient.getById(departmentId, currentTraceId()),
                    "getDepartmentById",
                    departmentId
            );
            return Optional.ofNullable(toDepartmentEntity(department));
        } catch (Exception e) {
            logger.error("Failed to get department by id: {}", departmentId, e);
            return Optional.empty();
        }
    }

    /**
     * 根据部门编码获取部门信息
     * @param departmentCode 部门编码
     * @return 部门实体
     */
    @Override
    public Optional<DepartmentEntity> getDepartmentByCode(String departmentCode) {
        try {
            HrDepartmentDTO department = unwrap(
                    hrDepartmentFeignClient.getByCode(departmentCode, currentTraceId()),
                    "getDepartmentByCode",
                    departmentCode
            );
            return Optional.ofNullable(toDepartmentEntity(department));
        } catch (Exception e) {
            logger.error("Failed to get department by code: {}", departmentCode, e);
            return Optional.empty();
        }
    }

    /**
     * 获取所有部门信息
     * @return 部门列表
     */
    @Override
    public List<DepartmentEntity> getAllDepartments() {
        try {
            List<HrDepartmentDTO> departments = unwrap(
                    hrDepartmentFeignClient.listAll(currentTraceId()),
                    "getAllDepartments",
                    null
            );
            return toDepartmentEntities(departments);
        } catch (Exception e) {
            logger.error("Failed to get all departments", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据部门ID获取部门员工列表
     * @param departmentId 部门ID
     * @return 部门员工列表
     */
    @Override
    public List<UserEntity> getEmployeesByDepartmentId(Long departmentId) {
        try {
            List<HrEmployeeDTO> employees = unwrap(
                    hrEmployeeFeignClient.listByDepartmentId(departmentId, currentTraceId()),
                    "getEmployeesByDepartmentId",
                    departmentId
            );
            return toUserEntities(employees);
        } catch (Exception e) {
            logger.error("Failed to get employees by department id: {}", departmentId, e);
            return new ArrayList<>();
        }
    }

    private <T> T unwrap(Result<T> result, String operation, Object identifier) {
        if (result == null) {
            logger.warn("HR integration returned empty response operation={} identifier={}", operation, identifier);
            return null;
        }
        if (!ResponseStatusAdapter.isSuccess(result.getCode())) {
            logger.warn(
                    "HR integration returned non-success code operation={} identifier={} code={} message={}",
                    operation,
                    identifier,
                    result.getCode(),
                    result.getMessage()
            );
            return null;
        }
        return result.getData();
    }

    private List<UserEntity> toUserEntities(List<HrEmployeeDTO> employees) {
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserEntity> users = new ArrayList<>(employees.size());
        for (HrEmployeeDTO employee : employees) {
            UserEntity user = toUserEntity(employee);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    private List<DepartmentEntity> toDepartmentEntities(List<HrDepartmentDTO> departments) {
        if (departments == null || departments.isEmpty()) {
            return Collections.emptyList();
        }
        List<DepartmentEntity> result = new ArrayList<>(departments.size());
        for (HrDepartmentDTO department : departments) {
            DepartmentEntity entity = toDepartmentEntity(department);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    private UserEntity toUserEntity(HrEmployeeDTO employee) {
        if (employee == null) {
            return null;
        }
        UserEntity user = new UserEntity();
        user.setId(employee.getId());
        user.setEmployeeId(employee.getId());
        user.setUsername(employee.getEmployeeCode());
        user.setRealName(employee.getName());
        user.setDepartmentId(employee.getDepartmentId());
        user.setDepartmentName(employee.getDepartmentName());
        user.setPosition(employee.getPositionName());
        user.setEmail(employee.getEmail());
        user.setPhone(employee.getPhone());
        user.setStatus(toEnabledFlag(employee.getStatus()));
        return user;
    }

    private DepartmentEntity toDepartmentEntity(HrDepartmentDTO department) {
        if (department == null) {
            return null;
        }
        DepartmentEntity entity = new DepartmentEntity();
        entity.setId(department.getId());
        entity.setName(department.getName());
        entity.setCode(department.getDepartmentCode());
        entity.setDescription(department.getDescription());
        entity.setParentId(department.getParentId());
        entity.setParentName(department.getParentName());
        entity.setLevel(department.getLevel() == null ? 1 : department.getLevel());
        entity.setSort(department.getSort() == null ? 0 : department.getSort());
        entity.setStatus(toEnabledFlag(department.getStatus()));
        entity.setManagerId(department.getManagerId());
        entity.setManagerName(department.getManagerName());
        return entity;
    }

    private Integer toEnabledFlag(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }

    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.MDC_TRACE_ID);
        return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
    }
}
