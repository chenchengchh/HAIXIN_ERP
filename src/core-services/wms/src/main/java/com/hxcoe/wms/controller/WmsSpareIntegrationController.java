package com.hxcoe.wms.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WMS备件领用集成控制器（EAM→WMS 备件领用库存联动闭环）
 * 接收 EAM 备件领用事件，按 备件编码+仓库编码 扣减 WMS 库存
 *
 * @author author
 * @date 2026-07-25
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/wms/integration", "/wms/integration"})
public class WmsSpareIntegrationController {

    /**
     * 默认备件仓库编码
     */
    private static final String DEFAULT_SPARE_WAREHOUSE = "WH-SPARE";

    private final InventoryRepository inventoryRepository;

    @Autowired
    public WmsSpareIntegrationController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * 接收EAM备件领用事件并扣减WMS库存
     * POST /api/v1/wms/integration/eam/spare-issue
     * 按 spareCode+warehouseCode 定位库存记录并扣减数量，库存不存在或不足时返回业务失败（HTTP 200）
     *
     * @param request 领用事件体（eventId/spareCode/spareName/quantity/warehouseCode/workOrderNo/issueTime）
     * @return 扣减结果（spareCode/deductQty/remainingQty）
     */
    @PostMapping("/eam/spare-issue")
    public Result<Map<String, Object>> deductSpareStock(@RequestBody SpareIssueDeductRequest request) {
        // 必填字段校验：备件编码、领用数量
        if (request == null || !StringUtils.hasText(request.getSpareCode())
                || request.getQuantity() == null || request.getQuantity() <= 0) {
            return Result.fail("备件领用参数不完整或数量非法");
        }
        String warehouseCode = StringUtils.hasText(request.getWarehouseCode())
                ? request.getWarehouseCode() : DEFAULT_SPARE_WAREHOUSE;

        // 按 备件编码+仓库编码 查询 WMS 库存（WMS库存物料编码字段为 materialCode）
        List<InventoryEntity> inventories = inventoryRepository
                .findByMaterialCodeAndWarehouseCode(request.getSpareCode(), warehouseCode);
        if (inventories == null || inventories.isEmpty()) {
            log.warn("WMS中无此备件库存: spareCode={}, warehouseCode={}, eventId={}",
                    request.getSpareCode(), warehouseCode, request.getEventId());
            return Result.fail("WMS中无此备件库存");
        }

        // 取该备件在目标仓库的首条库存记录扣减
        InventoryEntity inventory = inventories.get(0);
        BigDecimal deductQty = BigDecimal.valueOf(request.getQuantity());
        BigDecimal currentQty = inventory.getQuantity() == null ? BigDecimal.ZERO : inventory.getQuantity();
        BigDecimal remainingQty = currentQty.subtract(deductQty);
        // 校验扣减后不为负
        if (remainingQty.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("WMS备件库存不足: spareCode={}, warehouseCode={}, 当前库存={}, 需求={}",
                    request.getSpareCode(), warehouseCode, currentQty, deductQty);
            return Result.fail("WMS备件库存不足，当前库存=" + currentQty);
        }

        inventory.setQuantity(remainingQty);
        inventoryRepository.save(inventory);
        log.info("WMS备件库存扣减成功: spareCode={}, warehouseCode={}, 扣减={}, 剩余={}, workOrderNo={}, eventId={}",
                request.getSpareCode(), warehouseCode, deductQty, remainingQty,
                request.getWorkOrderNo(), request.getEventId());

        Map<String, Object> data = new HashMap<>();
        data.put("spareCode", request.getSpareCode());
        data.put("deductQty", request.getQuantity());
        data.put("remainingQty", remainingQty);
        return Result.success("扣减成功", data);
    }

    /**
     * EAM备件领用扣减请求体
     */
    @Data
    public static class SpareIssueDeductRequest {
        /**
         * 事件ID（UUID，用于链路追踪）
         */
        private String eventId;
        /**
         * 备件编码（对应WMS物料编码 materialCode）
         */
        private String spareCode;
        /**
         * 备件名称
         */
        private String spareName;
        /**
         * 领用数量
         */
        private Integer quantity;
        /**
         * 仓库编码（默认 WH-SPARE）
         */
        private String warehouseCode;
        /**
         * 维修工单号
         */
        private String workOrderNo;
        /**
         * 领用时间
         */
        private LocalDateTime issueTime;
    }
}
