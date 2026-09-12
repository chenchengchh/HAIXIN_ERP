package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.Opportunity;
import com.hxcoe.crm.service.OpportunityService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商机控制器
 */
@RestController
@RequestMapping("/api/v1/crm/opportunities")
public class OpportunityController {

    @Autowired
    private OpportunityService opportunityService;

    /**
     * 获取商机列表
     * @param page 页码
     * @param size 每页数量
     * @param opportunityName 商机名称
     * @param customerName 客户名称
     * @param stage 阶段
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Opportunity>> getOpportunitiesList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String opportunityName,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String status) {
        
        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Page<Opportunity> opportunitiesPage = opportunityService.getOpportunitiesList(
                opportunityName, customerName, stage, status, pageable);
        PageResult<Opportunity> pageResult = PageResult.build(
                opportunitiesPage.getTotalElements(),
                opportunitiesPage.getSize(),
                opportunitiesPage.getNumber() + 1,
                opportunitiesPage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 创建商机
     * @param opportunity 商机数据
     * @return 创建结果
     */
    @PostMapping
    public Result<Opportunity> createOpportunity(@RequestBody Opportunity opportunity) {
        Opportunity createdOpportunity = opportunityService.createOpportunity(opportunity);
        return Result.success("成功", createdOpportunity);
    }

    /**
     * 查询商机详情
     * @param id 商机ID
     * @return 商机详情
     */
    @GetMapping("/{id}")
    public Result<Opportunity> getOpportunityDetail(@PathVariable Long id) {
        Opportunity opportunity = opportunityService.getOpportunityDetail(id);
        return opportunity != null ? Result.success("成功", opportunity) : Result.fail("未找到");
    }

    /**
     * 更新商机
     * @param id 商机ID
     * @param opportunity 商机数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Opportunity> updateOpportunity(@PathVariable Long id, @RequestBody Opportunity opportunity) {
        Opportunity updatedOpportunity = opportunityService.updateOpportunity(id, opportunity);
        return updatedOpportunity != null ? Result.success("成功", updatedOpportunity) : Result.fail("未找到");
    }

    /**
     * 推进商机阶段
     * @param id 商机ID
     * @param data 阶段数据
     * @return 推进结果
     */
    @PostMapping("/{id}/stage")
    public Result<String> updateOpportunityStage(@PathVariable Long id, @RequestBody Object data) {
        String result = opportunityService.updateOpportunityStage(id, data);
        return Result.success("成功", result);
    }

    /**
     * 获取我的商机列表
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<PageResult<Opportunity>> getMyOpportunities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 这里可以从当前登录用户获取ownerId
        Long ownerId = 1L; // 临时固定值，实际应该从用户上下文获取
        
        Page<Opportunity> opportunitiesPage = opportunityService.getMyOpportunities(ownerId, pageable);
        PageResult<Opportunity> pageResult = PageResult.build(
                opportunitiesPage.getTotalElements(),
                opportunitiesPage.getSize(),
                opportunitiesPage.getNumber() + 1,
                opportunitiesPage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 获取销售漏斗分析（按阶段分组统计商机数量、预计金额及阶段转化率）
     * @return 销售漏斗数据列表，每项包含 stage/stageName/count/amount/conversionRate
     */
    @GetMapping("/funnel")
    public Result<List<Map<String, Object>>> getSalesFunnel() {
        List<Map<String, Object>> funnel = opportunityService.getSalesFunnel();
        return Result.success(funnel);
    }
}
