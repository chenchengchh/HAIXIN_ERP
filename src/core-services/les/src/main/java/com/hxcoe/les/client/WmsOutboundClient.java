package com.hxcoe.les.client;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "wms-service")
public interface WmsOutboundClient {

    /**
     * 查询WMS出库单列表（作为LES运输计划来源）
     *
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @param status     状态（可选）
     * @param orderType  类型（SALES/PRODUCTION/TRANSFER，可选）
     * @param outboundNo 出库单号（可选）
     * @param waveId     波次ID（可选）
     * @return 分页结果
     */
    @GetMapping("/api/v1/wms/outbound/orders")
    Result<PageResult<Map<String, Object>>> listOutboundOrders(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "outboundNo", required = false) String outboundNo,
            @RequestParam(value = "waveId", required = false) Long waveId
    );
}

