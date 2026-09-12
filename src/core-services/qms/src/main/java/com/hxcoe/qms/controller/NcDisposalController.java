package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcDisposalEntity;
import com.hxcoe.qms.service.NcDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 不合格品处理管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/nc-disposals")
public class NcDisposalController {

    @Autowired
    private NcDisposalService ncDisposalService;

    /**
     * 分页查询不合格品处理列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param registrationNo 登记编号（模糊）
     * @param disposalStatus 处理状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<NcDisposalEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String disposalStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return ncDisposalService.page(registrationNo, disposalStatus, pageable);
    }

    /**
     * 获取不合格品处理详情
     *
     * @param id 处理ID
     * @return 处理详情
     */
    @GetMapping("/{id}")
    public Result<NcDisposalEntity> getById(@PathVariable Long id) {
        return ncDisposalService.getById(id);
    }

    /**
     * 创建不合格品处理
     *
     * @param entity 处理数据
     * @return 创建结果
     */
    @PostMapping
    public Result<NcDisposalEntity> create(@RequestBody NcDisposalEntity entity) {
        return ncDisposalService.create(entity);
    }

    /**
     * 更新不合格品处理
     *
     * @param id 处理ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<NcDisposalEntity> update(@PathVariable Long id, @RequestBody NcDisposalEntity entity) {
        return ncDisposalService.update(id, entity);
    }

    /**
     * 开始处理
     *
     * @param id 处理ID
     * @return 处理结果
     */
    @PutMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        return ncDisposalService.start(id);
    }

    /**
     * 完成处理
     *
     * @param id 处理ID
     * @param body 处理结果数据
     * @return 处理结果
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String processResult = body == null ? null : (body.get("processResult") == null ? null : String.valueOf(body.get("processResult")));
        return ncDisposalService.complete(id, processResult);
    }

    /**
     * 批量开始处理
     *
     * @param body 批量处理数据
     * @return 处理结果
     */
    @PutMapping("/batch-start")
    public Result<Void> batchStart(@RequestBody Map<String, Object> body) {
        Object idsObj = body == null ? null : body.get("ids");
        List<Long> ids = idsObj instanceof List<?> list ? list.stream().map(v -> Long.valueOf(String.valueOf(v))).toList() : List.of();
        return ncDisposalService.batchStart(ids);
    }
}
