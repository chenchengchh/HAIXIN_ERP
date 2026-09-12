package com.hxcoe.erp.client;

import com.hxcoe.common.dto.mes.MesReportingCreateDTO;
import com.hxcoe.common.dto.mes.MesReportingUpdateDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ERP 调用 MES 报工服务的 Feign 客户端。
 *
 * <p>B3 修复：将弱类型 {@code Object body} 替换为强类型 DTO，明确接口契约。
 * 路径与 MES {@code ProductionReportController} 对齐（B2）：
 * POST /api/v1/mes/reporting/manual、PUT /api/v1/mes/reporting/{id}。
 */
@FeignClient(name = "mes-service", path = "/api/v1/mes/reporting", contextId = "erpMesReportingClient")
public interface MesReportingClient {

    /**
     * 分页查询报工列表。
     *
     * @return 报工列表（结果体由调用方按 Map 解析）
     */
    @GetMapping("/list")
    Result<Object> list(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "reportNo", required = false) String reportNo,
            @RequestParam(value = "workOrderNo", required = false) String workOrderNo,
            @RequestParam(value = "operatorName", required = false) String operatorName,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    );

    /**
     * 手动创建报工（B3 强类型契约）。
     *
     * @param body 报工创建 DTO
     * @return 创建结果
     */
    @PostMapping("/manual")
    Result<Object> manual(@RequestBody MesReportingCreateDTO body);

    /**
     * 按 ID 更新报工（B3 强类型契约，增量更新）。
     *
     * @param id  报工ID
     * @param body 报工更新 DTO
     * @return 更新结果
     */
    @PutMapping("/{id}")
    Result<Object> update(@PathVariable("id") Long id, @RequestBody MesReportingUpdateDTO body);
}
