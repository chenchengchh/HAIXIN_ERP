package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcReviewEntity;
import com.hxcoe.qms.service.NcReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 不合格品评审管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/nc-reviews")
public class NcReviewController {

    @Autowired
    private NcReviewService ncReviewService;

    /**
     * 分页查询不合格品评审列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param registrationNo 登记编号（模糊）
     * @param reviewStatus 评审状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<NcReviewEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String reviewStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return ncReviewService.page(registrationNo, reviewStatus, pageable);
    }

    /**
     * 获取不合格品评审详情
     *
     * @param id 评审ID
     * @return 评审详情
     */
    @GetMapping("/{id}")
    public Result<NcReviewEntity> getById(@PathVariable Long id) {
        return ncReviewService.getById(id);
    }

    /**
     * 创建不合格品评审
     *
     * @param entity 评审数据
     * @return 创建结果
     */
    @PostMapping
    public Result<NcReviewEntity> create(@RequestBody NcReviewEntity entity) {
        return ncReviewService.create(entity);
    }

    /**
     * 更新不合格品评审
     *
     * @param id 评审ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<NcReviewEntity> update(@PathVariable Long id, @RequestBody NcReviewEntity entity) {
        return ncReviewService.update(id, entity);
    }

    /**
     * 审批不合格品评审
     *
     * @param id 评审ID
     * @param body 审批数据
     * @return 审批结果
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reviewStatus = body == null ? null : String.valueOf(body.get("reviewStatus"));
        String reviewOpinion = body == null ? null : (body.get("reviewOpinion") == null ? null : String.valueOf(body.get("reviewOpinion")));
        return ncReviewService.approve(id, reviewStatus, reviewOpinion);
    }

    /**
     * 批量审批不合格品评审
     *
     * @param body 批量审批数据
     * @return 处理结果
     */
    @PutMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody Map<String, Object> body) {
        Object idsObj = body == null ? null : body.get("ids");
        List<Long> ids = idsObj instanceof List<?> list ? list.stream().map(v -> Long.valueOf(String.valueOf(v))).toList() : List.of();
        String reviewStatus = body == null ? null : String.valueOf(body.get("reviewStatus"));
        String reviewOpinion = body == null ? null : (body.get("reviewOpinion") == null ? null : String.valueOf(body.get("reviewOpinion")));
        return ncReviewService.batchApprove(ids, reviewStatus, reviewOpinion);
    }
}
