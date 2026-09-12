package com.hxcoe.hr.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.hr.dto.DepartmentDTO;
import com.hxcoe.hr.entity.DepartmentEntity;
import com.hxcoe.hr.service.DepartmentService;
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
 * 部门控制器
 */
@RestController
@RequestMapping("/api/v1/hr/departments")
@Tag(name = "部门管理", description = "部门信息管理相关接口")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * 创建部门
     *
     * @param department 部门实体
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建部门", description = "创建新的部门信息")
    public Result<DepartmentDTO> createDepartment(@RequestBody DepartmentEntity department) {
        logger.info("创建部门: {}", department.getName());
        DepartmentEntity createdDepartment = departmentService.createDepartment(department);
        DepartmentDTO createdDepartmentDTO = modelMapper.map(createdDepartment, DepartmentDTO.class);
        return Result.success(createdDepartmentDTO);
    }

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param department 部门实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新部门信息", description = "根据ID更新部门信息")
    public Result<DepartmentDTO> updateDepartment(@PathVariable Long id, @RequestBody DepartmentEntity department) {
        logger.info("更新部门信息: id={}", id);
        DepartmentEntity updatedDepartment = departmentService.updateDepartment(id, department);
        DepartmentDTO updatedDepartmentDTO = modelMapper.map(updatedDepartment, DepartmentDTO.class);
        return Result.success(updatedDepartmentDTO);
    }

    /**
     * 根据ID删除部门
     *
     * @param id 部门ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        logger.info("删除部门: id={}", id);
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询部门", description = "根据ID查询部门信息")
    public Result<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        logger.info("查询部门: id={}", id);
        return departmentService.getDepartmentById(id)
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .map(Result::success)
                .orElse(Result.fail("部门不存在"));
    }

    /**
     * 根据部门编号查询部门
     *
     * @param departmentCode 部门编号
     * @return 查询结果
     */
    @GetMapping("/code/{departmentCode}")
    @Operation(summary = "根据编号查询部门", description = "根据部门编号查询部门信息")
    public Result<DepartmentDTO> getDepartmentByCode(@PathVariable String departmentCode) {
        logger.info("根据编号查询部门: departmentCode={}", departmentCode);
        return departmentService.getDepartmentByCode(departmentCode)
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .map(Result::success)
                .orElse(Result.fail("部门不存在"));
    }

    /**
     * 查询所有部门
     *
     * @return 查询结果
     */
    @GetMapping
    @Operation(summary = "查询所有部门", description = "获取所有部门列表")
    public Result<List<DepartmentDTO>> getAllDepartments() {
        logger.info("查询所有部门");
        List<DepartmentEntity> departments = departmentService.getAllDepartments();
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 分页查询部门
     *
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询部门", description = "分页查询部门列表")
    public Result<PageResult<DepartmentDTO>> getDepartmentsByPage(@PageableDefault(size = 10) Pageable pageable) {
        logger.info("分页查询部门: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<DepartmentEntity> page = departmentService.getDepartmentsByPage(pageable);
        
        List<DepartmentDTO> departmentDTOs = page.getContent().stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        
        PageResult<DepartmentDTO> pageResult = PageResult.build(
                page.getTotalElements(),
                page.getSize(),
                page.getNumber() + 1, // Pageable的page从0开始，前端从1开始
                departmentDTOs
        );
        return Result.success(pageResult);
    }

    /**
     * 查询根部门列表
     *
     * @return 根部门列表
     */
    @GetMapping("/root")
    @Operation(summary = "查询根部门", description = "获取所有根部门列表")
    public Result<List<DepartmentDTO>> getRootDepartments() {
        logger.info("查询根部门");
        List<DepartmentEntity> departments = departmentService.getRootDepartments();
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子部门", description = "根据父部门ID查询子部门列表")
    public Result<List<DepartmentDTO>> getChildDepartments(@PathVariable Long parentId) {
        logger.info("查询子部门: parentId={}", parentId);
        List<DepartmentEntity> departments = departmentService.getChildDepartments(parentId);
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 根据部门状态查询部门列表
     *
     * @param status 部门状态
     * @return 部门列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询部门", description = "根据部门状态查询部门列表")
    public Result<List<DepartmentDTO>> getDepartmentsByStatus(@PathVariable String status) {
        logger.info("根据状态查询部门: status={}", status);
        List<DepartmentEntity> departments = departmentService.getDepartmentsByStatus(status);
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 搜索部门
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索部门", description = "根据关键词搜索部门")
    public Result<List<DepartmentDTO>> searchDepartments(@RequestParam String keyword) {
        logger.info("搜索部门: keyword={}", keyword);
        List<DepartmentEntity> departments = departmentService.searchDepartments(keyword);
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 查询部门及其所有子部门
     *
     * @param id 部门ID
     * @return 部门及其子部门列表
     */
    @GetMapping("/tree/{id}")
    @Operation(summary = "查询部门树", description = "查询部门及其所有子部门")
    public Result<List<DepartmentDTO>> getDepartmentWithChildren(@PathVariable Long id) {
        logger.info("查询部门树: id={}", id);
        List<DepartmentEntity> departments = departmentService.getDepartmentWithChildren(id);
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(department -> modelMapper.map(department, DepartmentDTO.class))
                .collect(Collectors.toList());
        return Result.success(departmentDTOs);
    }

    /**
     * 设置部门负责人
     *
     * @param departmentId 部门ID
     * @param managerId 部门负责人ID
     * @return 设置结果
     */
    @PutMapping("/{departmentId}/manager/{managerId}")
    @Operation(summary = "设置部门负责人", description = "设置部门的负责人")
    public Result<DepartmentDTO> setDepartmentManager(@PathVariable Long departmentId, @PathVariable Long managerId) {
        logger.info("设置部门负责人: departmentId={}, managerId={}", departmentId, managerId);
        DepartmentEntity department = departmentService.setDepartmentManager(departmentId, managerId);
        DepartmentDTO departmentDTO = modelMapper.map(department, DepartmentDTO.class);
        return Result.success(departmentDTO);
    }

    /**
     * 批量删除部门
     *
     * @param ids 部门ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除部门", description = "批量删除部门")
    public Result<Void> batchDeleteDepartments(@RequestBody List<Long> ids) {
        logger.info("批量删除部门: count={}", ids.size());
        departmentService.batchDeleteDepartments(ids);
        return Result.success();
    }
}
