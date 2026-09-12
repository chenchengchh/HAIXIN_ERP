package com.hxcoe.hr.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.hr.dto.EmployeeDTO;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/v1/hr/employees")
@Tag(name = "员工管理", description = "员工信息管理相关接口")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 创建员工
     *
     * @param employee 员工实体
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建员工", description = "创建新的员工信息")
    public Result<EmployeeDTO> createEmployee(@RequestBody EmployeeEntity employee) {
        logger.info("创建员工: {}", employee.getName());
        EmployeeEntity createdEmployee = employeeService.createEmployee(employee);
        EmployeeDTO createdEmployeeDTO = modelMapper.map(createdEmployee, EmployeeDTO.class);
        return Result.success(createdEmployeeDTO);
    }

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param employee 员工实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新员工信息", description = "根据ID更新员工信息")
    public Result<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeEntity employee) {
        logger.info("更新员工信息: id={}", id);
        EmployeeEntity updatedEmployee = employeeService.updateEmployee(id, employee);
        EmployeeDTO updatedEmployeeDTO = modelMapper.map(updatedEmployee, EmployeeDTO.class);
        return Result.success(updatedEmployeeDTO);
    }

    /**
     * 根据ID删除员工
     *
     * @param id 员工ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工", description = "根据ID删除员工")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        logger.info("删除员工: id={}", id);
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询员工", description = "根据ID查询员工信息")
    public Result<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        logger.info("查询员工: id={}", id);
        return employeeService.getEmployeeById(id)
                .map(employee -> {
                    EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
                    return Result.success(employeeDTO);
                })
                .orElse(Result.fail("员工不存在"));
    }

    /**
     * 根据员工编号查询员工
     *
     * @param employeeCode 员工编号
     * @return 查询结果
     */
    @GetMapping("/code/{employeeCode}")
    @Operation(summary = "根据编号查询员工", description = "根据员工编号查询员工信息")
    public Result<EmployeeDTO> getEmployeeByCode(@PathVariable String employeeCode) {
        logger.info("根据编号查询员工: employeeCode={}", employeeCode);
        return employeeService.getEmployeeByCode(employeeCode)
                .map(employee -> {
                    EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
                    return Result.success(employeeDTO);
                })
                .orElse(Result.fail("员工不存在"));
    }

    /**
     * 查询所有员工
     *
     * @return 查询结果
     */
    @GetMapping
    @Operation(summary = "查询所有员工", description = "获取所有员工列表")
    public Result<List<EmployeeDTO>> getAllEmployees() {
        logger.info("查询所有员工");
        List<EmployeeEntity> employees = employeeService.getAllEmployees();
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(employeeDTOs);
    }

    /**
     * 分页查询员工
     *
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询员工", description = "分页查询员工列表")
    public Result<PageResult<EmployeeDTO>> getEmployeesByPage(@PageableDefault(size = 10) Pageable pageable) {
        logger.info("分页查询员工: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<EmployeeEntity> page = employeeService.getEmployeesByPage(pageable);
        
        List<EmployeeDTO> employeeDTOs = page.getContent().stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        
        PageResult<EmployeeDTO> pageResult = PageResult.build(
                page.getTotalElements(),
                page.getSize(),
                page.getNumber() + 1, // Pageable的page从0开始，前端从1开始
                employeeDTOs
        );
        return Result.success(pageResult);
    }

    /**
     * 根据部门ID查询员工
     *
     * @param departmentId 部门ID
     * @return 查询结果
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门查询员工", description = "根据部门ID查询员工列表")
    public Result<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        logger.info("根据部门查询员工: departmentId={}", departmentId);
        List<EmployeeEntity> employees = employeeService.getEmployeesByDepartment(departmentId);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(employeeDTOs);
    }

    /**
     * 根据岗位ID查询员工
     *
     * @param positionId 岗位ID
     * @return 查询结果
     */
    @GetMapping("/position/{positionId}")
    @Operation(summary = "根据岗位查询员工", description = "根据岗位ID查询员工列表")
    public Result<List<EmployeeDTO>> getEmployeesByPosition(@PathVariable Long positionId) {
        logger.info("根据岗位查询员工: positionId={}", positionId);
        List<EmployeeEntity> employees = employeeService.getEmployeesByPosition(positionId);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(employeeDTOs);
    }

    /**
     * 根据员工状态查询员工
     *
     * @param status 员工状态
     * @return 查询结果
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询员工", description = "根据员工状态查询员工列表")
    public Result<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable String status) {
        logger.info("根据状态查询员工: status={}", status);
        List<EmployeeEntity> employees = employeeService.getEmployeesByStatus(status);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(employeeDTOs);
    }

    /**
     * 搜索员工
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索员工", description = "根据关键词搜索员工")
    public Result<List<EmployeeDTO>> searchEmployees(@RequestParam String keyword) {
        logger.info("搜索员工: keyword={}", keyword);
        List<EmployeeEntity> employees = employeeService.searchEmployees(keyword);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(employeeDTOs);
    }

    /**
     * 查询部门负责人
     *
     * @param departmentId 部门ID
     * @return 查询结果
     */
    @GetMapping("/manager/{departmentId}")
    @Operation(summary = "查询部门负责人", description = "根据部门ID查询部门负责人")
    public Result<EmployeeDTO> getDepartmentManager(@PathVariable Long departmentId) {
        logger.info("查询部门负责人: departmentId={}", departmentId);
        return employeeService.getDepartmentManager(departmentId)
                .map(employee -> {
                    EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
                    return Result.success(employeeDTO);
                })
                .orElse(Result.fail("部门负责人不存在"));
    }

    /**
     * 批量导入员工
     *
     * @param employees 员工列表
     * @return 导入结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量导入员工", description = "批量导入员工信息")
    public Result<List<EmployeeDTO>> batchImportEmployees(@RequestBody List<EmployeeEntity> employees) {
        logger.info("批量导入员工: count={}", employees.size());
        List<EmployeeEntity> importedEmployees = employeeService.batchImportEmployees(employees);
        List<EmployeeDTO> importedEmployeeDTOs = importedEmployees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        return Result.success(importedEmployeeDTOs);
    }

    /**
     * 批量删除员工
     *
     * @param ids 员工ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除员工", description = "批量删除员工")
    public Result<Void> batchDeleteEmployees(@RequestBody List<Long> ids) {
        logger.info("批量删除员工: count={}", ids.size());
        employeeService.batchDeleteEmployees(ids);
        return Result.success();
    }
}
