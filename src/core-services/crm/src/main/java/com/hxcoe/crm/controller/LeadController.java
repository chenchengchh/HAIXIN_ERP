package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.Lead;
import com.hxcoe.crm.service.LeadService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 销售线索控制器
 */
@RestController
@RequestMapping("/api/v1/crm/leads")
public class LeadController {

    @Autowired
    private LeadService leadService;

    /**
     * 获取线索列表
     * @param page 页码
     * @param size 每页数量
     * @param leadName 线索名称
     * @param status 状态
     * @param rating 评级
     * @param ownerName 负责人姓名
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Lead>> getLeadsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String leadName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String ownerName) {
        
        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Page<Lead> leadsPage = leadService.getLeadsList(leadName, status, rating, ownerName, pageable);
        PageResult<Lead> pageResult = PageResult.build(
                leadsPage.getTotalElements(),
                leadsPage.getSize(),
                leadsPage.getNumber() + 1,
                leadsPage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 创建销售线索
     * @param lead 线索数据
     * @return 创建结果
     */
    @PostMapping
    public Result<Lead> createLead(@RequestBody Lead lead) {
        Lead createdLead = leadService.createLead(lead);
        return Result.success("成功", createdLead);
    }

    /**
     * 查询线索详情
     * @param id 线索ID
     * @return 线索详情
     */
    @GetMapping("/{id}")
    public Result<Lead> getLeadDetail(@PathVariable Long id) {
        Lead lead = leadService.getLeadDetail(id);
        return lead != null ? Result.success("成功", lead) : Result.fail("未找到");
    }

    /**
     * 更新线索
     * @param id 线索ID
     * @param lead 线索数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Lead> updateLead(@PathVariable Long id, @RequestBody Lead lead) {
        Lead updatedLead = leadService.updateLead(id, lead);
        return updatedLead != null ? Result.success("成功", updatedLead) : Result.fail("未找到");
    }

    /**
     * 将线索转化为客户
     * @param id 线索ID
     * @param data 转化数据
     * @return 转化结果
     */
    @PostMapping("/{id}/convert")
    public Result<String> convertLead(@PathVariable Long id, @RequestBody Object data) {
        String result = leadService.convertLead(id, data);
        return Result.success("成功", result);
    }
}
