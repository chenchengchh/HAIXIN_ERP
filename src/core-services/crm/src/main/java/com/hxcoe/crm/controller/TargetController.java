package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.SalesTarget;
import com.hxcoe.crm.service.SalesTargetService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 销售目标控制器
 */
@RestController
@RequestMapping("/api/v1/crm/targets")
public class TargetController {

    @Autowired
    private SalesTargetService salesTargetService;

    /**
     * 获取我的销售目标
     * @param page 页码
     * @param size 每页数量
     * @param ownerName 负责人姓名
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<PageResult<SalesTarget>> getMyTargets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String ownerName) {

        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<SalesTarget> targetPage = salesTargetService.getMyTargets(ownerName, pageable);
        PageResult<SalesTarget> pageResult = PageResult.build(
                targetPage.getTotalElements(),
                targetPage.getSize(),
                targetPage.getNumber() + 1,
                targetPage.getContent()
        );
        return Result.success(pageResult);
    }
}
