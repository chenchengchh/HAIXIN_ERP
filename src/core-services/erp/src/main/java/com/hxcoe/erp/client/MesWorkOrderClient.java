package com.hxcoe.erp.client;

import com.hxcoe.common.dto.mes.WorkOrderStatusUpdateDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ERP 调用 MES 工单服务的 Feign 客户端。
 *
 * <p>B3 修复：将弱类型 {@code Object body} 替换为强类型 DTO，明确接口契约。
 * 工单创建走 B1 的 Outbox 集成入口（{@code MesIntegrationClient}），此处保留
 * list/create 供 ERP 生产订单页面直接查询/创建车间工单。
 */
@FeignClient(name = "mes-service", path = "/api/v1/mes/work-orders", contextId = "erpMesWorkOrderClient")
public interface MesWorkOrderClient {

    /**
     * 查询工单列表。
     *
     * @return 工单列表（结果体由调用方按 Map 解析）
     */
    @GetMapping
    Result<Object> list();

    /**
     * 创建工单（车间工单直接创建，非 B1 集成事件路径）。
     *
     * @param body 工单实体字段（与 MES WorkOrderEntity 结构对齐）
     * @return 创建结果
     */
    @PostMapping
    Result<Object> create(@RequestBody Object body);

    /**
     * 按工单号更新工单状态（B3 强类型契约）。
     *
     * @param workOrderNo 工单号
     * @param body        状态更新 DTO（status/actualQuantity/operatorName/remark）
     * @return 更新结果
     */
    @PutMapping("/no/{workOrderNo}/status")
    Result<Object> updateStatusByNo(@PathVariable("workOrderNo") String workOrderNo,
                                    @RequestBody WorkOrderStatusUpdateDTO body);
}
