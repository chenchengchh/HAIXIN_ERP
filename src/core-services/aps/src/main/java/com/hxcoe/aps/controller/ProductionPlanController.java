package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.service.ApsApprovalIntegrationService;
import com.hxcoe.aps.service.ProductionPlanService;
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
@RequestMapping("/api/v1/aps/production-plans")
@Tag(name = "生产计划管理", description = "生产计划管理相关接口")
public class ProductionPlanController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionPlanController.class);

    @Autowired
    private ProductionPlanService productionPlanService;

    @Autowired
    private ApsApprovalIntegrationService approvalIntegrationService;

    @GetMapping
    @Operation(summary = "获取所有生产计划", description = "获取所有生产计划列表")
    public Result<List<ProductionPlanEntity>> getProductionPlans() {
        logger.info("获取所有生产计划");
        List<ProductionPlanEntity> plans = productionPlanService.getAllProductionPlans();
        return Result.success(plans);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取生产计划", description = "分页查询生产计划列表")
    public Result<PageResult<ProductionPlanEntity>> getProductionPlansByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        logger.info("分页获取生产计划: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        var pageResult = productionPlanService.getProductionPlansByPage(pageable);
        PageResult<ProductionPlanEntity> result = PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent()
        );
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询生产计划", description = "根据ID查询生产计划")
    public Result<ProductionPlanEntity> getProductionPlanById(@PathVariable Long id) {
        logger.info("查询生产计划: id={}", id);
        ProductionPlanEntity plan = productionPlanService.getProductionPlanById(id);
        return Result.success(plan);
    }

    @GetMapping("/no/{planNo}")
    @Operation(summary = "根据编号查询生产计划", description = "根据计划编号查询生产计划")
    public Result<ProductionPlanEntity> getProductionPlanByNo(@PathVariable String planNo) {
        logger.info("根据编号查询生产计划: planNo={}", planNo);
        ProductionPlanEntity plan = productionPlanService.getProductionPlanByPlanNo(planNo);
        return Result.success(plan);
    }

    @PostMapping
    @Operation(summary = "创建生产计划", description = "创建新的生产计划")
    public Result<ProductionPlanEntity> createProductionPlan(@RequestBody ProductionPlanEntity productionPlan) {
        logger.info("创建生产计划");
        ProductionPlanEntity createdPlan = productionPlanService.createProductionPlan(productionPlan);
        return Result.success(createdPlan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新生产计划", description = "根据ID更新生产计划")
    public Result<ProductionPlanEntity> updateProductionPlan(
            @PathVariable Long id,
            @RequestBody ProductionPlanEntity productionPlan) {
        logger.info("更新生产计划: id={}", id);
        ProductionPlanEntity updatedPlan = productionPlanService.updateProductionPlan(id, productionPlan);
        return Result.success(updatedPlan);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除生产计划", description = "根据ID删除生产计划")
    public Result<Void> deleteProductionPlan(@PathVariable Long id) {
        logger.info("删除生产计划: id={}", id);
        productionPlanService.deleteProductionPlan(id);
        return Result.success();
    }

    /**
     * 提交生产计划审批（发起OA统一审批）。
     * <p>计划状态置为submitted（审批中），同时向OA提交审批申请，
     * 审批完成后OA回调更新计划状态为approved/rejected。</p>
     *
     * @param id            计划ID
     * @param initiatorId   发起人ID（前端传入当前登录用户ID）
     * @param initiatorName 发起人名称（前端传入当前登录用户名称）
     * @return 提交结果
     */
    @PostMapping("/{id}/submit-approval")
    @Operation(summary = "提交生产计划审批", description = "将生产计划提交到OA统一审批中心")
    public Result<ProductionPlanEntity> submitPlanForApproval(
            @PathVariable Long id,
            @RequestParam(required = false) Long initiatorId,
            @RequestParam(required = false) String initiatorName) {
        logger.info("提交生产计划审批: id={}, initiatorId={}, initiatorName={}", id, initiatorId, initiatorName);
        
        ProductionPlanEntity plan = productionPlanService.getProductionPlanById(id);
        if (plan == null) {
            return Result.error("生产计划不存在");
        }
        
        // 仅草稿（draft）状态允许提交审批，防止重复提交
        if (!"draft".equals(plan.getStatus())) {
            return Result.error("当前状态不允许提交审批");
        }
        
        boolean submitted = approvalIntegrationService.submitProductionPlanApproval(
                plan.getId(), plan.getPlanNo(), plan.getPlanName(),
                plan.getProductName(), plan.getQuantity(),
                plan.getStartTime() != null ? plan.getStartTime().toString() : null,
                plan.getEndTime() != null ? plan.getEndTime().toString() : null,
                initiatorId, initiatorName);
        
        if (submitted) {
            // 更新计划状态为审批中
            plan.setStatus("submitted");
            productionPlanService.updateProductionPlan(id, plan);
            return Result.success("生产计划已提交审批", plan);
        } else {
            return Result.error("提交审批失败，请稍后重试");
        }
    }
}
