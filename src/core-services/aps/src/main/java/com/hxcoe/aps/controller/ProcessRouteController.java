package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ProcessRouteEntity;
import com.hxcoe.aps.service.ProcessRouteService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps")
@Tag(name = "工艺路线管理", description = "工艺路线管理相关接口")
public class ProcessRouteController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRouteController.class);

    @Autowired
    private ProcessRouteService processRouteService;

    @PostMapping("/process-routes")
    @Operation(summary = "创建工艺路线", description = "创建新的工艺路线")
    public Result<ProcessRouteEntity> createProcessRoute(@RequestBody ProcessRouteEntity processRoute) {
        logger.info("创建工艺路线");
        return Result.success(processRouteService.createProcessRoute(processRoute));
    }

    @GetMapping("/process-routes/{id}")
    @Operation(summary = "查询工艺路线", description = "根据ID查询工艺路线")
    public Result<ProcessRouteEntity> getProcessRoute(@PathVariable Long id) {
        logger.info("查询工艺路线: id={}", id);
        return Result.success(processRouteService.getProcessRouteById(id));
    }

    @GetMapping("/process-routes")
    @Operation(summary = "获取所有工艺路线", description = "获取所有工艺路线列表")
    public Result<List<ProcessRouteEntity>> getAllProcessRoutes() {
        logger.info("获取所有工艺路线");
        return Result.success(processRouteService.getAllProcessRoutes());
    }

    @PutMapping("/process-routes/{id}")
    @Operation(summary = "更新工艺路线", description = "根据ID更新工艺路线")
    public Result<ProcessRouteEntity> updateProcessRoute(
            @PathVariable Long id,
            @RequestBody ProcessRouteEntity processRoute) {
        logger.info("更新工艺路线: id={}", id);
        return Result.success(processRouteService.updateProcessRoute(id, processRoute));
    }

    @DeleteMapping("/process-routes/{id}")
    @Operation(summary = "删除工艺路线", description = "根据ID删除工艺路线")
    public Result<Void> deleteProcessRoute(@PathVariable Long id) {
        logger.info("删除工艺路线: id={}", id);
        processRouteService.deleteProcessRoute(id);
        return Result.success();
    }
}
