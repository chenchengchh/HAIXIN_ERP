package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ResourceCalendarEntity;
import com.hxcoe.aps.service.ResourceCalendarService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/resource-calendars")
@Tag(name = "资源日历管理", description = "资源日历管理相关接口")
public class ResourceCalendarController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceCalendarController.class);

    @Autowired
    private ResourceCalendarService resourceCalendarService;

    @GetMapping
    @Operation(summary = "获取所有资源日历", description = "获取所有资源日历列表")
    public Result<List<ResourceCalendarEntity>> getAllResourceCalendars() {
        logger.info("获取所有资源日历");
        return Result.success(resourceCalendarService.getAllResourceCalendars());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询资源日历", description = "根据ID查询资源日历")
    public Result<ResourceCalendarEntity> getResourceCalendarById(@PathVariable Long id) {
        logger.info("查询资源日历: id={}", id);
        ResourceCalendarEntity resourceCalendar = resourceCalendarService.getResourceCalendarById(id);
        return Result.success(resourceCalendar);
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "根据资源ID查询日历", description = "根据资源ID查询资源日历")
    public Result<List<ResourceCalendarEntity>> getResourceCalendarsByResourceId(@PathVariable Long resourceId) {
        logger.info("根据资源ID查询日历: resourceId={}", resourceId);
        List<ResourceCalendarEntity> calendars = resourceCalendarService.getResourceCalendarsByResourceId(resourceId);
        return Result.success(calendars);
    }

    @GetMapping("/resource/{resourceId}/date-range")
    @Operation(summary = "查询资源日历日期范围", description = "根据资源ID和日期范围查询资源日历")
    public Result<List<ResourceCalendarEntity>> getResourceCalendarsByResourceIdAndDateRange(
            @PathVariable Long resourceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("查询资源日历日期范围: resourceId={}, startDate={}, endDate={}", resourceId, startDate, endDate);
        List<ResourceCalendarEntity> calendars = resourceCalendarService.getResourceCalendarsByResourceIdAndDateRange(resourceId, startDate, endDate);
        return Result.success(calendars);
    }

    @PostMapping
    @Operation(summary = "创建资源日历", description = "创建新的资源日历")
    public Result<ResourceCalendarEntity> createResourceCalendar(@RequestBody ResourceCalendarEntity resourceCalendar) {
        logger.info("创建资源日历");
        ResourceCalendarEntity createdCalendar = resourceCalendarService.createResourceCalendar(resourceCalendar);
        return Result.success(createdCalendar);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源日历", description = "根据ID更新资源日历")
    public Result<ResourceCalendarEntity> updateResourceCalendar(
            @PathVariable Long id,
            @RequestBody ResourceCalendarEntity resourceCalendar) {
        logger.info("更新资源日历: id={}", id);
        ResourceCalendarEntity updatedCalendar = resourceCalendarService.updateResourceCalendar(id, resourceCalendar);
        return Result.success(updatedCalendar);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源日历", description = "根据ID删除资源日历")
    public Result<Void> deleteResourceCalendar(@PathVariable Long id) {
        logger.info("删除资源日历: id={}", id);
        resourceCalendarService.deleteResourceCalendar(id);
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量设置资源日历", description = "批量设置资源日历")
    public Result<List<ResourceCalendarEntity>> batchSetResourceCalendars(@RequestBody List<ResourceCalendarEntity> resourceCalendars) {
        logger.info("批量设置资源日历: count={}", resourceCalendars.size());
        List<ResourceCalendarEntity> calendars = resourceCalendarService.batchSetResourceCalendars(resourceCalendars);
        return Result.success(calendars);
    }

    @GetMapping("/available/{resourceId}/{date}")
    @Operation(summary = "检查资源可用性", description = "检查指定日期资源是否可用")
    public Result<Boolean> isResourceAvailableOnDate(
            @PathVariable Long resourceId,
            @PathVariable LocalDate date) {
        logger.info("检查资源可用性: resourceId={}, date={}", resourceId, date);
        boolean available = resourceCalendarService.isResourceAvailableOnDate(resourceId, date);
        return Result.success(available);
    }

    @GetMapping("/available-minutes/{resourceId}/{date}")
    @Operation(summary = "获取可用分钟数", description = "获取指定日期资源的可用分钟数")
    public Result<Integer> getAvailableMinutesOnDate(
            @PathVariable Long resourceId,
            @PathVariable LocalDate date) {
        logger.info("获取可用分钟数: resourceId={}, date={}", resourceId, date);
        Integer availableMinutes = resourceCalendarService.getAvailableMinutesOnDate(resourceId, date);
        return Result.success(availableMinutes);
    }
}
