package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.AlgorithmParamEntity;
import com.hxcoe.aps.service.AlgorithmParamService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/algorithm-params")
@Tag(name = "算法参数管理", description = "算法参数管理相关接口")
public class AlgorithmParamController {

    private static final Logger logger = LoggerFactory.getLogger(AlgorithmParamController.class);

    @Autowired
    private AlgorithmParamService algorithmParamService;

    @GetMapping
    @Operation(summary = "获取所有算法参数", description = "获取所有算法参数配置列表")
    public Result<List<AlgorithmParamEntity>> getAlgorithmParams() {
        logger.info("获取所有算法参数");
        List<AlgorithmParamEntity> params = algorithmParamService.getAlgorithmParams();
        return Result.success(params);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取算法参数", description = "分页查询算法参数")
    public Result<PageResult<AlgorithmParamEntity>> getAlgorithmParamsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        logger.info("分页获取算法参数: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        var pageResult = algorithmParamService.getAlgorithmParamsByPage(pageable);
        PageResult<AlgorithmParamEntity> result = PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent()
        );
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询算法参数", description = "根据ID查询算法参数")
    public Result<AlgorithmParamEntity> getAlgorithmParamById(@PathVariable Long id) {
        logger.info("查询算法参数: id={}", id);
        AlgorithmParamEntity param = algorithmParamService.getAlgorithmParamById(id);
        return Result.success(param);
    }

    @GetMapping("/algorithm/{algorithmName}")
    @Operation(summary = "根据算法名称查询参数", description = "根据算法名称查询算法参数")
    public Result<List<AlgorithmParamEntity>> getAlgorithmParamsByAlgorithmName(@PathVariable String algorithmName) {
        logger.info("根据算法名称查询参数: algorithmName={}", algorithmName);
        List<AlgorithmParamEntity> params = algorithmParamService.getAlgorithmParamsByAlgorithmName(algorithmName);
        return Result.success(params);
    }

    @PostMapping
    @Operation(summary = "创建算法参数", description = "创建新的算法参数")
    public Result<AlgorithmParamEntity> createAlgorithmParam(@RequestBody AlgorithmParamEntity param) {
        logger.info("创建算法参数");
        AlgorithmParamEntity createdParam = algorithmParamService.createAlgorithmParam(param);
        return Result.success(createdParam);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新算法参数", description = "根据ID更新算法参数")
    public Result<AlgorithmParamEntity> updateAlgorithmParam(
            @PathVariable Long id,
            @RequestBody AlgorithmParamEntity param) {
        logger.info("更新算法参数: id={}", id);
        AlgorithmParamEntity updatedParam = algorithmParamService.updateAlgorithmParam(id, param);
        return Result.success(updatedParam);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除算法参数", description = "根据ID删除算法参数")
    public Result<Void> deleteAlgorithmParam(@PathVariable Long id) {
        logger.info("删除算法参数: id={}", id);
        algorithmParamService.deleteAlgorithmParam(id);
        return Result.success();
    }
}
