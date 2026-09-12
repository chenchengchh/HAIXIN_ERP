package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcTrackingEntity;
import com.hxcoe.qms.service.NcTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 不合格品追踪管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/nc-trackings")
public class NcTrackingController {

    @Autowired
    private NcTrackingService ncTrackingService;

    /**
     * 分页查询不合格品追踪列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param registrationNo 登记编号（模糊）
     * @param trackingStatus 追踪状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<NcTrackingEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String trackingStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return ncTrackingService.page(registrationNo, trackingStatus, pageable);
    }

    /**
     * 获取不合格品追踪详情
     *
     * @param id 追踪ID
     * @return 追踪详情
     */
    @GetMapping("/{id}")
    public Result<NcTrackingEntity> getById(@PathVariable Long id) {
        return ncTrackingService.getById(id);
    }

    /**
     * 创建不合格品追踪
     *
     * @param entity 追踪数据
     * @return 创建结果
     */
    @PostMapping
    public Result<NcTrackingEntity> create(@RequestBody NcTrackingEntity entity) {
        return ncTrackingService.create(entity);
    }

    /**
     * 更新不合格品追踪
     *
     * @param id 追踪ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<NcTrackingEntity> update(@PathVariable Long id, @RequestBody NcTrackingEntity entity) {
        return ncTrackingService.update(id, entity);
    }

    /**
     * 开始追踪
     *
     * @param id 追踪ID
     * @return 处理结果
     */
    @PutMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        return ncTrackingService.start(id);
    }

    /**
     * 完成追踪
     *
     * @param id 追踪ID
     * @param body 追踪结果数据
     * @return 处理结果
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String effectiveness = body == null ? null : (body.get("effectiveness") == null ? null : String.valueOf(body.get("effectiveness")));
        String improvementSuggestions = body == null ? null : (body.get("improvementSuggestions") == null ? null : String.valueOf(body.get("improvementSuggestions")));
        return ncTrackingService.complete(id, effectiveness, improvementSuggestions);
    }
}
