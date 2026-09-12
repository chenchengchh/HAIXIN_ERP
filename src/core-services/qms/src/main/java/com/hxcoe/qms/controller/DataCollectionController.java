package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.DataCollectionEntity;
import com.hxcoe.qms.service.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

/**
 * 质量数据采集管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/data-collections")
public class DataCollectionController {

    @Autowired
    private DataCollectionService dataCollectionService;

    /**
     * 分页查询质量数据采集列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param collectionNo 采集编号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<DataCollectionEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String collectionNo,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String collectionDateStart,
            @RequestParam(required = false) String collectionDateEnd,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        LocalDate start = parseDateOrNull(collectionDateStart);
        LocalDate end = parseDateOrNull(collectionDateEnd);
        return dataCollectionService.page(collectionNo, dataType, start, end, status, pageable);
    }

    private static LocalDate parseDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取质量数据采集详情
     *
     * @param id 采集ID
     * @return 采集详情
     */
    @GetMapping("/{id}")
    public Result<DataCollectionEntity> getById(@PathVariable Long id) {
        return dataCollectionService.getById(id);
    }

    /**
     * 创建质量数据采集
     *
     * @param entity 采集数据
     * @return 创建结果
     */
    @PostMapping
    public Result<DataCollectionEntity> create(@RequestBody DataCollectionEntity entity) {
        return dataCollectionService.create(entity);
    }

    /**
     * 更新质量数据采集
     *
     * @param id 采集ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<DataCollectionEntity> update(@PathVariable Long id, @RequestBody DataCollectionEntity entity) {
        return dataCollectionService.update(id, entity);
    }

    /**
     * 提交质量数据采集
     *
     * @param id 采集ID
     * @return 提交结果
     */
    @PutMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        return dataCollectionService.submit(id);
    }

    /**
     * 删除质量数据采集
     *
     * @param id 采集ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return dataCollectionService.delete(id);
    }

    /**
     * 导入质量数据
     *
     * @param file 导入文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importData(@RequestPart("file") MultipartFile file) {
        return dataCollectionService.importData(file);
    }
}
