package com.hxcoe.hr.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.hr.dto.PositionDTO;
import com.hxcoe.hr.entity.PositionEntity;
import com.hxcoe.hr.service.PositionService;
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
 * 岗位控制器
 */
@RestController
@RequestMapping("/api/v1/hr/positions")
@Tag(name = "岗位管理", description = "岗位信息管理相关接口")
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    @Autowired
    private PositionService positionService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * 创建岗位
     *
     * @param position 岗位实体
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建岗位", description = "创建新的岗位信息")
    public Result<PositionDTO> createPosition(@RequestBody PositionEntity position) {
        logger.info("创建岗位: {}", position.getName());
        PositionEntity createdPosition = positionService.createPosition(position);
        PositionDTO createdPositionDTO = modelMapper.map(createdPosition, PositionDTO.class);
        return Result.success(createdPositionDTO);
    }

    /**
     * 更新岗位信息
     *
     * @param id 岗位ID
     * @param position 岗位实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新岗位信息", description = "根据ID更新岗位信息")
    public Result<PositionDTO> updatePosition(@PathVariable Long id, @RequestBody PositionEntity position) {
        logger.info("更新岗位信息: id={}", id);
        PositionEntity updatedPosition = positionService.updatePosition(id, position);
        PositionDTO updatedPositionDTO = modelMapper.map(updatedPosition, PositionDTO.class);
        return Result.success(updatedPositionDTO);
    }

    /**
     * 根据ID删除岗位
     *
     * @param id 岗位ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除岗位", description = "根据ID删除岗位")
    public Result<Void> deletePosition(@PathVariable Long id) {
        logger.info("删除岗位: id={}", id);
        positionService.deletePosition(id);
        return Result.success();
    }

    /**
     * 根据ID查询岗位
     *
     * @param id 岗位ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询岗位", description = "根据ID查询岗位信息")
    public Result<PositionDTO> getPositionById(@PathVariable Long id) {
        logger.info("查询岗位: id={}", id);
        return positionService.getPositionById(id)
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .map(Result::success)
                .orElse(Result.fail("岗位不存在"));
    }

    /**
     * 根据岗位编号查询岗位
     *
     * @param positionCode 岗位编号
     * @return 查询结果
     */
    @GetMapping("/code/{positionCode}")
    @Operation(summary = "根据编号查询岗位", description = "根据岗位编号查询岗位信息")
    public Result<PositionDTO> getPositionByCode(@PathVariable String positionCode) {
        logger.info("根据编号查询岗位: positionCode={}", positionCode);
        return positionService.getPositionByCode(positionCode)
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .map(Result::success)
                .orElse(Result.fail("岗位不存在"));
    }

    /**
     * 查询所有岗位
     *
     * @return 查询结果
     */
    @GetMapping
    @Operation(summary = "查询所有岗位", description = "获取所有岗位列表")
    public Result<List<PositionDTO>> getAllPositions() {
        logger.info("查询所有岗位");
        List<PositionEntity> positions = positionService.getAllPositions();
        List<PositionDTO> positionDTOs = positions.stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        return Result.success(positionDTOs);
    }

    /**
     * 分页查询岗位
     *
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询岗位", description = "分页查询岗位列表")
    public Result<PageResult<PositionDTO>> getPositionsByPage(@PageableDefault(size = 10) Pageable pageable) {
        logger.info("分页查询岗位: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<PositionEntity> page = positionService.getPositionsByPage(pageable);
        
        List<PositionDTO> positionDTOs = page.getContent().stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        PageResult<PositionDTO> pageResult = PageResult.build(
                page.getTotalElements(),
                page.getSize(),
                page.getNumber() + 1, // Pageable的page从0开始，前端从1开始
                positionDTOs
        );
        return Result.success(pageResult);
    }

    /**
     * 根据岗位等级查询岗位列表
     *
     * @param level 岗位等级
     * @return 岗位列表
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "根据等级查询岗位", description = "根据岗位等级查询岗位列表")
    public Result<List<PositionDTO>> getPositionsByLevel(@PathVariable String level) {
        logger.info("根据等级查询岗位: level={}", level);
        List<PositionEntity> positions = positionService.getPositionsByLevel(level);
        List<PositionDTO> positionDTOs = positions.stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        return Result.success(positionDTOs);
    }

    /**
     * 根据岗位状态查询岗位列表
     *
     * @param status 岗位状态
     * @return 岗位列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询岗位", description = "根据岗位状态查询岗位列表")
    public Result<List<PositionDTO>> getPositionsByStatus(@PathVariable String status) {
        logger.info("根据状态查询岗位: status={}", status);
        List<PositionEntity> positions = positionService.getPositionsByStatus(status);
        List<PositionDTO> positionDTOs = positions.stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        return Result.success(positionDTOs);
    }

    /**
     * 根据岗位等级和状态查询岗位列表
     *
     * @param level 岗位等级
     * @param status 岗位状态
     * @return 岗位列表
     */
    @GetMapping("/level/{level}/status/{status}")
    @Operation(summary = "根据等级和状态查询岗位", description = "根据岗位等级和状态查询岗位列表")
    public Result<List<PositionDTO>> getPositionsByLevelAndStatus(@PathVariable String level, @PathVariable String status) {
        logger.info("根据等级和状态查询岗位: level={}, status={}", level, status);
        List<PositionEntity> positions = positionService.getPositionsByLevelAndStatus(level, status);
        List<PositionDTO> positionDTOs = positions.stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        return Result.success(positionDTOs);
    }

    /**
     * 搜索岗位
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索岗位", description = "根据关键词搜索岗位")
    public Result<List<PositionDTO>> searchPositions(@RequestParam String keyword) {
        logger.info("搜索岗位: keyword={}", keyword);
        List<PositionEntity> positions = positionService.searchPositions(keyword);
        List<PositionDTO> positionDTOs = positions.stream()
                .map(position -> modelMapper.map(position, PositionDTO.class))
                .collect(Collectors.toList());
        return Result.success(positionDTOs);
    }

    /**
     * 批量删除岗位
     *
     * @param ids 岗位ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除岗位", description = "批量删除岗位")
    public Result<Void> batchDeletePositions(@RequestBody List<Long> ids) {
        logger.info("批量删除岗位: count={}", ids.size());
        positionService.batchDeletePositions(ids);
        return Result.success();
    }
}
