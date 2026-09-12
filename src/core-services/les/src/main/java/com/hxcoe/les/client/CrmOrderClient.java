package com.hxcoe.les.client;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "crm-service")
public interface CrmOrderClient {

    /**
     * 查询CRM销售订单列表（作为LES运输计划来源补充）
     *
     * @param page         页码（从1开始）
     * @param size         每页条数
     * @param keyword      关键字（可选）
     * @param status       状态（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param customerName 客户名称（可选）
     * @return 分页结果
     */
    @GetMapping("/api/v1/crm/orders/my")
    Result<PageResult<Map<String, Object>>> listMyOrders(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "customerName", required = false) String customerName
    );
}

