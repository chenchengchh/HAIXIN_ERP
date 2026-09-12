package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.entity.*;
import com.hxcoe.wms.repository.*;
import com.hxcoe.wms.service.AsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/wms/pda")
public class PdaScanController {

    @Autowired
    private AsnRepository asnRepository;

    @Autowired
    private AsnService asnService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private PickingTaskRepository pickingTaskRepository;

    @Autowired
    private PickingTaskItemRepository pickingTaskItemRepository;

    /**
     * PDA 上架：按任务号（ASN号）查询任务信息
     */
    @GetMapping("/inbound/task")
    public ApiResponse<Map<String, Object>> getInboundTask(@RequestParam String taskNo) {
        if (taskNo == null || taskNo.isBlank()) {
            return badRequest("taskNo不能为空");
        }
        AsnEntity asn = asnRepository.findByAsnNo(taskNo.trim());
        if (asn == null) {
            return notFound("上架任务不存在");
        }
        return success("查询成功", toInboundTask(asn));
    }

    /**
     * PDA 上架：确认上架（更新 ASN 收货数量 + 增加库存 + 写入库存流水）
     */
    @PostMapping("/inbound/{asnId}/putaway")
    public ApiResponse<Map<String, Object>> confirmPutaway(@PathVariable Long asnId, @RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String barcode = String.valueOf(body.getOrDefault("barcode", body.getOrDefault("materialCode", "")));
        String locationCode = String.valueOf(body.getOrDefault("locationCode", ""));
        BigDecimal quantity = parseQuantity(body.get("quantity"));
        String operator = String.valueOf(body.getOrDefault("operator", "PDA"));

        if (barcode == null || barcode.isBlank()) return badRequest("barcode不能为空");
        if (locationCode == null || locationCode.isBlank()) return badRequest("locationCode不能为空");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) return badRequest("quantity必须大于0");

        LocationEntity location = locationRepository.findByLocationCode(locationCode.trim());
        if (location == null) {
            return notFound("库位不存在");
        }

        AsnEntity asn = asnRepository.findById(asnId).orElse(null);
        if (asn == null) {
            return notFound("上架任务不存在");
        }

        AsnItemEntity matchItem = findAsnItem(asn, barcode.trim());
        if (matchItem == null) {
            return badRequest("商品条码不在该任务中");
        }

        AsnEntity updatedAsn = asnService.scanAsn(asnId, barcode.trim(), quantity);
        if (updatedAsn == null) {
            return badRequest("上架失败");
        }

        String warehouseCode = (updatedAsn.getWarehouseCode() == null || updatedAsn.getWarehouseCode().isBlank()) ? "MAIN_WH" : updatedAsn.getWarehouseCode();
        String batchNo = matchItem.getBatchNo() == null ? "" : matchItem.getBatchNo();
        String unit = matchItem.getUnit() == null ? "" : matchItem.getUnit();

        Optional<InventoryEntity> optionalInv = inventoryRepository.findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo(
                barcode.trim(), warehouseCode, locationCode.trim(), batchNo
        );
        InventoryEntity inv = optionalInv.orElseGet(InventoryEntity::new);
        inv.setWarehouseCode(warehouseCode);
        inv.setLocationCode(locationCode.trim());
        inv.setMaterialCode(barcode.trim());
        inv.setMaterialName(matchItem.getMaterialName());
        inv.setBatchNo(batchNo);
        inv.setUnit(unit);
        BigDecimal current = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
        inv.setQuantity(current.add(quantity));
        inventoryRepository.save(inv);

        InventoryTransactionEntity tx = new InventoryTransactionEntity();
        tx.setType("INBOUND");
        tx.setWarehouseCode(warehouseCode);
        tx.setLocationCode(locationCode.trim());
        tx.setMaterialCode(barcode.trim());
        tx.setMaterialName(matchItem.getMaterialName());
        tx.setQuantity(quantity);
        tx.setUnit(unit);
        tx.setBatchNo(batchNo);
        tx.setSourceNo(updatedAsn.getAsnNo());
        tx.setTransactionTime(LocalDateTime.now());
        tx.setOperator(operator);
        inventoryTransactionRepository.save(tx);

        Map<String, Object> result = new HashMap<>();
        result.put("task", toInboundTask(updatedAsn));
        result.put("record", Map.of(
                "materialCode", barcode.trim(),
                "materialName", matchItem.getMaterialName() == null ? "" : matchItem.getMaterialName(),
                "locationCode", locationCode.trim(),
                "quantity", quantity,
                "shelveTime", LocalDateTime.now().toString(),
                "status", "success"
        ));
        return success("上架成功", result);
    }

    /**
     * PDA 出库：按任务号查询拣货任务
     */
    @GetMapping("/outbound/task")
    public ApiResponse<Map<String, Object>> getOutboundTask(@RequestParam String taskNo) {
        if (taskNo == null || taskNo.isBlank()) {
            return badRequest("taskNo不能为空");
        }
        PickingTaskEntity task = pickingTaskRepository.findByTaskNo(taskNo.trim()).orElse(null);
        if (task == null) {
            return notFound("拣货任务不存在");
        }
        List<PickingTaskItemEntity> items = pickingTaskItemRepository.findByTaskId(task.getId());
        return success("查询成功", toOutboundTask(task, items));
    }

    /**
     * PDA 出库：确认拣货（扣减库存 + 写入库存流水 + 更新拣货明细状态）
     */
    @PostMapping("/outbound/{taskId}/pick")
    public ApiResponse<Map<String, Object>> confirmPick(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String barcode = String.valueOf(body.getOrDefault("barcode", body.getOrDefault("materialCode", "")));
        String locationCode = String.valueOf(body.getOrDefault("locationCode", ""));
        BigDecimal quantity = parseQuantity(body.get("quantity"));
        String operator = String.valueOf(body.getOrDefault("operator", "PDA"));
        String warehouseCode = String.valueOf(body.getOrDefault("warehouseCode", "MAIN_WH"));

        if (barcode == null || barcode.isBlank()) return badRequest("barcode不能为空");
        if (locationCode == null || locationCode.isBlank()) return badRequest("locationCode不能为空");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) return badRequest("quantity必须大于0");

        PickingTaskEntity task = pickingTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return notFound("拣货任务不存在");
        }

        List<PickingTaskItemEntity> items = pickingTaskItemRepository.findByTaskId(task.getId());
        PickingTaskItemEntity matchItem = items.stream()
                .filter(i -> i.getMaterialCode() != null && i.getMaterialCode().equalsIgnoreCase(barcode.trim()))
                .findFirst()
                .orElse(null);
        if (matchItem == null) {
            return badRequest("商品条码不在该任务中");
        }

        String batchNo = matchItem.getBatchNo() == null ? "" : matchItem.getBatchNo();
        Optional<InventoryEntity> optionalInv = inventoryRepository.findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo(
                barcode.trim(), warehouseCode, locationCode.trim(), batchNo
        );
        InventoryEntity inv = optionalInv.orElse(null);
        if (inv == null || inv.getQuantity() == null) {
            return notFound("库存不存在");
        }
        BigDecimal current = inv.getQuantity();
        BigDecimal newQty = current.subtract(quantity);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            return badRequest("库存不足");
        }
        inv.setQuantity(newQty);
        inventoryRepository.save(inv);

        InventoryTransactionEntity tx = new InventoryTransactionEntity();
        tx.setType("OUTBOUND");
        tx.setWarehouseCode(warehouseCode);
        tx.setLocationCode(locationCode.trim());
        tx.setMaterialCode(barcode.trim());
        tx.setMaterialName(matchItem.getMaterialName());
        tx.setQuantity(quantity);
        tx.setUnit(matchItem.getUnit());
        tx.setBatchNo(batchNo);
        tx.setSourceNo(task.getTaskNo());
        tx.setTransactionTime(LocalDateTime.now());
        tx.setOperator(operator);
        inventoryTransactionRepository.save(tx);

        matchItem.setStatus("completed");
        matchItem.setPickTime(LocalDateTime.now());
        pickingTaskItemRepository.save(matchItem);

        Map<String, Object> result = new HashMap<>();
        result.put("record", Map.of(
                "materialCode", barcode.trim(),
                "materialName", matchItem.getMaterialName() == null ? "" : matchItem.getMaterialName(),
                "locationCode", locationCode.trim(),
                "quantity", quantity,
                "pickTime", LocalDateTime.now().toString(),
                "status", "success"
        ));
        result.put("task", toOutboundTask(task, pickingTaskItemRepository.findByTaskId(task.getId())));
        return success("拣货成功", result);
    }

    private static BigDecimal parseQuantity(Object v) {
        if (v == null) return BigDecimal.ONE;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception ignored) {
            return BigDecimal.ONE;
        }
    }

    private static AsnItemEntity findAsnItem(AsnEntity asn, String materialCode) {
        if (asn == null || asn.getItems() == null) return null;
        for (AsnItemEntity i : asn.getItems()) {
            if (i != null && i.getMaterialCode() != null && i.getMaterialCode().equalsIgnoreCase(materialCode)) {
                return i;
            }
        }
        return null;
    }

    private static Map<String, Object> toInboundTask(AsnEntity asn) {
        Map<String, Object> data = new HashMap<>();
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal received = BigDecimal.ZERO;
        if (asn.getItems() != null) {
            for (AsnItemEntity item : asn.getItems()) {
                if (item == null) continue;
                expected = expected.add(item.getExpectedQuantity() == null ? BigDecimal.ZERO : item.getExpectedQuantity());
                received = received.add(item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity());
            }
        }
        BigDecimal remaining = expected.subtract(received);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        data.put("id", asn.getId() == null ? "" : String.valueOf(asn.getId()));
        data.put("taskNo", asn.getAsnNo() == null ? "" : asn.getAsnNo());
        data.put("orderNo", asn.getDeliveryNoteNo() == null ? "" : asn.getDeliveryNoteNo());
        data.put("expectedQuantity", expected);
        data.put("shelvedQuantity", received);
        data.put("remainingQuantity", remaining);
        data.put("status", mapInboundStatus(asn.getStatus()));
        return data;
    }

    private static String mapInboundStatus(String status) {
        if (status == null) return "pending";
        String s = status.trim().toUpperCase();
        if (s.equals("RECEIVED") || s.equals("CLOSED")) return "completed";
        if (s.equals("CANCELLED")) return "cancelled";
        if (s.equals("CREATED")) return "pending";
        return "in_progress";
    }

    private static Map<String, Object> toOutboundTask(PickingTaskEntity task, List<PickingTaskItemEntity> items) {
        Map<String, Object> data = new HashMap<>();
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal picked = BigDecimal.ZERO;
        List<Map<String, Object>> products = new ArrayList<>();
        if (items != null) {
            for (PickingTaskItemEntity i : items) {
                if (i == null) continue;
                BigDecimal qty = i.getQuantity() == null ? BigDecimal.ZERO : i.getQuantity();
                expected = expected.add(qty);
                boolean completed = "completed".equalsIgnoreCase(i.getStatus());
                if (completed) picked = picked.add(qty);
                Map<String, Object> p = new HashMap<>();
                p.put("materialCode", i.getMaterialCode() == null ? "" : i.getMaterialCode());
                p.put("materialName", i.getMaterialName() == null ? "" : i.getMaterialName());
                p.put("specification", "");
                p.put("expectedQuantity", qty);
                p.put("pickedQuantity", completed ? qty : BigDecimal.ZERO);
                p.put("remainingQuantity", completed ? BigDecimal.ZERO : qty);
                p.put("locationCode", i.getLocationCode() == null ? "" : i.getLocationCode());
                p.put("status", completed ? "completed" : "pending");
                products.add(p);
            }
        }
        BigDecimal remaining = expected.subtract(picked);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        data.put("id", task.getId() == null ? "" : String.valueOf(task.getId()));
        data.put("taskNo", task.getTaskNo() == null ? "" : task.getTaskNo());
        data.put("orderNo", task.getOrderNo() == null ? "" : task.getOrderNo());
        data.put("waveNo", task.getWaveNo() == null ? "" : task.getWaveNo());
        data.put("expectedQuantity", expected);
        data.put("pickedQuantity", picked);
        data.put("remainingQuantity", remaining);
        data.put("priority", "medium");
        data.put("status", mapOutboundStatus(task.getStatus()));
        data.put("products", products);
        return data;
    }

    private static String mapOutboundStatus(String status) {
        if (status == null) return "pending";
        String s = status.trim().toLowerCase();
        if (s.equals("completed")) return "completed";
        if (s.equals("cancelled")) return "cancelled";
        if (s.equals("assigned") || s.equals("picking")) return "in_progress";
        return "pending";
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

