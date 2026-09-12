package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.PurchaseOrderService;
import com.hxcoe.scm.service.ScmPoAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/purchase-orders", "/scm/purchase-orders"})
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @Autowired
    private ScmPoAuditService scmPoAuditService;

    @Autowired
    private SrmClient srmClient;

    @PostMapping
    public ApiResponse<PurchaseOrderEntity> create(@RequestBody PurchaseOrderEntity order) {
        return ResultAdapter.fromResult(purchaseOrderService.createOrder(order));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderEntity> update(@PathVariable Long id, @RequestBody PurchaseOrderEntity order) {
        return ResultAdapter.fromResult(purchaseOrderService.updateOrder(id, order));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ResultAdapter.fromResult(purchaseOrderService.deleteOrder(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderEntity> getById(@PathVariable Long id) {
        return ResultAdapter.fromResult(purchaseOrderService.getOrderById(id));
    }

    @GetMapping("/by-no/{orderNo}")
    public ApiResponse<PurchaseOrderEntity> getByOrderNo(@PathVariable String orderNo) {
        return ResultAdapter.fromResult(purchaseOrderService.getOrderByOrderNo(orderNo));
    }

    @GetMapping("/by-no/{orderNo}/items")
    public ApiResponse<List<PurchaseOrderItemEntity>> getItemsByOrderNo(@PathVariable String orderNo) {
        return success("采购订单明细查询成功", purchaseOrderItemRepository.findByOrderNo(orderNo));
    }

    @PostMapping("/by-no/{orderNo}/supplier/confirm")
    public ApiResponse<PurchaseOrderEntity> supplierConfirm(@PathVariable String orderNo, @RequestBody SupplierConfirmRequest req) {
        PurchaseOrderEntity order = purchaseOrderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return notFound("采购订单不存在");
        }
        Integer beforeStatus = order.getOrderStatus();
        if (req != null && req.getCommitDeliveryDate() != null) {
            order.setExpectedDeliveryDate(req.getCommitDeliveryDate());
        }
        order.setOrderStatus(40);
        order.setUpdatedBy(req != null && req.getOperator() != null ? req.getOperator() : "srm");
        order.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity saved = purchaseOrderRepository.save(order);
        purchaseOrderEventOutboxService.enqueuePoEvent(saved.getOrderNo(), "PO_CONFIRMED");
        scmPoAuditService.upsertSupplierCommit(saved.getOrderNo(), saved.getSupplierCode(), req == null ? null : req.getCommitDeliveryDate(), "CONFIRMED",
                saved.getUpdatedBy(),
                Map.of(
                        "commitDeliveryDate", req == null ? null : req.getCommitDeliveryDate(),
                        "operator", saved.getUpdatedBy()
                )
        );
        if (saved.getOrderStatus() != null && (beforeStatus == null || !saved.getOrderStatus().equals(beforeStatus))) {
            scmPoAuditService.recordStatusChange(saved.getOrderNo(), beforeStatus, saved.getOrderStatus(), "PO_CONFIRMED", saved.getUpdatedBy(), Map.of(
                    "source", "srm",
                    "action", "SUPPLIER_CONFIRM"
            ));
        }
        return success("供应商确认成功", saved);
    }

    @GetMapping
    public ApiResponse<PageResult<PurchaseOrderEntity>> getOrders(@RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer size) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(p - 1, s);
        return ResultAdapter.fromResult(purchaseOrderService.getOrdersByPage(pageable));
    }

    @PostMapping("/import-from-srm")
    public ApiResponse<Map<String, Object>> importFromSrm() {
        int importedOrders = 0;
        int importedItems = 0;
        int page = 0;
        int size = 200;

        while (true) {
            Result<Map<String, Object>> res = srmClient.getPurchaseOrders(page, size);
            if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode()) || res.getData() == null) {
                break;
            }
            Object contentObj = res.getData().get("content");
            if (!(contentObj instanceof List<?> content) || content.isEmpty()) {
                break;
            }

            for (Object rowObj : content) {
                if (!(rowObj instanceof Map<?, ?> r)) continue;
                Object srmIdObj = r.get("id");
                Long srmId = srmIdObj instanceof Number n ? n.longValue() : null;
                String orderNo = r.get("orderNo") == null ? null : String.valueOf(r.get("orderNo"));
                if (orderNo == null || orderNo.isBlank()) continue;

                PurchaseOrderEntity order = purchaseOrderRepository.findByOrderNo(orderNo).orElse(null);
                if (order == null) {
                    order = new PurchaseOrderEntity();
                    order.setOrderNo(orderNo);
                    order.setSupplierId(toLong(r.get("supplierId")));
                    order.setSupplierName(toString(r.get("supplierName")));
                    order.setSupplierCode("UNKNOWN");
                    order.setOrderAmount(toBigDecimal(r.get("totalAmount")));
                    order.setExpectedDeliveryDate(parseDateTime(r.get("expectedDeliveryDate")));
                    order.setOrderStatus(mapSrmStatusToScm(r.get("status")));
                    order.setPurchaseType(0);
                    order.setRemark(toString(r.get("remarks")));
                    order.setCreatedBy("system-migration");
                    order.setUpdatedBy("system-migration");
                    order.setCreatedTime(parseDateTime(r.get("createdTime")) != null ? parseDateTime(r.get("createdTime")) : LocalDateTime.now());
                    order.setUpdatedTime(parseDateTime(r.get("updatedTime")) != null ? parseDateTime(r.get("updatedTime")) : LocalDateTime.now());
                    order = purchaseOrderRepository.save(order);
                    importedOrders++;
                }

                if (srmId == null) continue;
                Result<Map<String, Object>> detailRes = srmClient.getPurchaseOrderById(srmId);
                if (detailRes == null || !ResponseStatusAdapter.isSuccess(detailRes.getCode()) || detailRes.getData() == null) {
                    continue;
                }
                Object itemsObj = detailRes.getData().get("items");
                if (!(itemsObj instanceof List<?> items)) {
                    continue;
                }

                purchaseOrderItemRepository.deleteByOrderNo(orderNo);
                List<PurchaseOrderItemEntity> newItems = new ArrayList<>();
                for (Object itemObj : items) {
                    if (!(itemObj instanceof Map<?, ?> it)) continue;
                    PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
                    item.setOrderId(order.getId());
                    item.setOrderNo(orderNo);
                    item.setMaterialCode(toString(it.get("materialCode")));
                    item.setMaterialName(toString(it.get("materialName")));
                    item.setUnit(toString(it.get("unit")));
                    item.setQuantity(toBigDecimal(it.get("quantity")));
                    item.setUnitPrice(toBigDecimal(it.get("unitPrice")));
                    item.setAmount(toBigDecimal(it.get("subtotal")));
                    item.setReceivedQuantity(toBigDecimal(it.get("receivedQuantity")));
                    item.setCreatedBy("system-migration");
                    item.setUpdatedBy("system-migration");
                    item.setCreatedTime(LocalDateTime.now());
                    item.setUpdatedTime(LocalDateTime.now());
                    newItems.add(item);
                }
                if (!newItems.isEmpty()) {
                    purchaseOrderItemRepository.saveAll(newItems);
                    importedItems += newItems.size();
                }
            }
            page++;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("importedOrders", importedOrders);
        data.put("importedItems", importedItems);
        return success("导入成功", data);
    }

    public static class SupplierConfirmRequest {
        private LocalDateTime commitDeliveryDate;
        private String operator;

        public LocalDateTime getCommitDeliveryDate() {
            return commitDeliveryDate;
        }

        public void setCommitDeliveryDate(LocalDateTime commitDeliveryDate) {
            this.commitDeliveryDate = commitDeliveryDate;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }

    private Integer mapSrmStatusToScm(Object statusObj) {
        String s = toString(statusObj);
        if (s == null) return 10;
        return switch (s) {
            case "CREATED" -> 10;
            case "SUBMITTED" -> 20;
            case "APPROVED" -> 30;
            case "SENT" -> 35;
            case "CONFIRMED" -> 40;
            case "PARTIAL_RECEIVED", "PARTIALLY_RECEIVED", "PARTIAL" -> 50;
            case "COMPLETED", "RECEIVED" -> 60;
            case "CLOSED" -> 70;
            case "CANCELLED" -> 90;
            default -> 10;
        };
    }

    private String toString(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s;
    }

    private Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        if (v == null) return null;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime t) return t;
        try {
            return LocalDateTime.parse(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
