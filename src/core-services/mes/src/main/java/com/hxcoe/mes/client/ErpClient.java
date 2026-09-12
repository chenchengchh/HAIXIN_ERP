package com.hxcoe.mes.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MES 调用 ERP 服务的 Feign 客户端。
 *
 * <p>B4 修复：新增按生产单号（业务键）回写状态的方法，替代原先误用 MES 工单主键的调用。
 * 保留 {@link #updateProductionStatus} 仅供历史路径兼容，新代码应使用
 * {@link #updateProductionStatusByNo}。
 */
@FeignClient(name = "erp-service")
public interface ErpClient {

    /**
     * 按 ERP 生产单主键回写状态（历史路径，保留兼容）。
     *
     * @param id              ERP 生产单主键
     * @param status          外部状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量
     * @return 操作结果
     */
    @PutMapping("/api/v1/erp/production/{id}/status")
    Result<Void> updateProductionStatus(
        @PathVariable("id") Long id,
        @RequestParam("status") String status,
        @RequestParam(value = "actualQuantity", required = false) Double actualQuantity
    );

    /**
     * 按 ERP 生产单号回写状态（B4 修复：使用业务键，MES 侧持此键）。
     *
     * @param productionNo    ERP 生产单号（业务键）
     * @param status          外部状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量
     * @return 操作结果
     */
    @PutMapping("/api/v1/erp/production/no/{productionNo}/status")
    Result<Void> updateProductionStatusByNo(
        @PathVariable("productionNo") String productionNo,
        @RequestParam("status") String status,
        @RequestParam(value = "actualQuantity", required = false) Double actualQuantity
    );
}
