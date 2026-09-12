package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.scm.client.dto.srm.SupplierEventRequest;
import com.hxcoe.scm.client.dto.wms.LocationEventRequest;
import com.hxcoe.scm.client.dto.wms.OutboundShippedRequest;
import com.hxcoe.scm.client.dto.wms.WarehouseEventRequest;
import com.hxcoe.scm.client.dto.erp.PoFactRequest;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.ErpPoFactOutboxService;
import com.hxcoe.scm.service.MesCompletionFactService;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.ScmOutboundShipmentFactService;
import com.hxcoe.scm.service.SupplierMirrorService;
import com.hxcoe.scm.service.WarehouseLocationMirrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/scm/integration", "/scm/integration"})
public class ScmIntegrationController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @Autowired
    private SupplierMirrorService supplierMirrorService;

    @Autowired
    private WarehouseLocationMirrorService warehouseLocationMirrorService;

    @Autowired
    private ErpPoFactOutboxService erpPoFactOutboxService;

    @Autowired
    private ScmOutboundShipmentFactService scmOutboundShipmentFactService;

    @Autowired
    private MesCompletionFactService mesCompletionFactService;

    @PostMapping("/srm/supplier-events")
    public ApiResponse<Map<String, Object>> receiveSupplierEvents(@RequestBody SupplierEventRequest req) {
        supplierMirrorService.applySrmEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return success("成功", data);
    }

    @PostMapping("/wms/receipt-completed")
    public ApiResponse<Map<String, Object>> receiptCompleted(@RequestBody ReceiptCompletedRequest req) {
        if (req == null || req.getPoNo() == null || req.getPoNo().isBlank()) {
            return badRequest("poNo不能为空");
        }
        if (req.getReceiptNo() == null || req.getReceiptNo().isBlank()) {
            return badRequest("receiptNo不能为空");
        }
        if (req.getLines() == null || req.getLines().isEmpty()) {
            return badRequest("lines不能为空");
        }
        PurchaseOrderEntity order = purchaseOrderRepository.findByOrderNo(req.getPoNo()).orElse(null);
        if (order == null) {
            return notFound("采购订单不存在");
        }

        int updatedItems = 0;
        for (ReceiptLine line : req.getLines()) {
            if (line == null || line.getMaterialCode() == null || line.getMaterialCode().isBlank() || line.getQty() == null) {
                continue;
            }
            List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNoAndMaterialCode(req.getPoNo(), line.getMaterialCode());
            if (items.isEmpty()) continue;
            PurchaseOrderItemEntity item = items.get(0);
            BigDecimal received = item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity();
            BigDecimal newReceived = received.add(line.getQty());
            if (item.getQuantity() != null && newReceived.compareTo(item.getQuantity()) > 0) {
                newReceived = item.getQuantity();
            }
            item.setReceivedQuantity(newReceived);
            item.setUpdatedBy("wms");
            item.setUpdatedTime(LocalDateTime.now());
            purchaseOrderItemRepository.save(item);
            updatedItems++;
        }

        List<PurchaseOrderItemEntity> allItems = purchaseOrderItemRepository.findByOrderNo(req.getPoNo());
        boolean anyReceived = false;
        boolean allReceived = !allItems.isEmpty();
        for (PurchaseOrderItemEntity it : allItems) {
            BigDecimal qty = it.getQuantity();
            BigDecimal rcv = it.getReceivedQuantity() == null ? BigDecimal.ZERO : it.getReceivedQuantity();
            if (rcv.compareTo(BigDecimal.ZERO) > 0) {
                anyReceived = true;
            }
            if (qty != null && rcv.compareTo(qty) < 0) {
                allReceived = false;
            }
        }

        if (allReceived) {
            order.setOrderStatus(60);
        } else if (anyReceived) {
            order.setOrderStatus(50);
        }
        if (anyReceived && order.getActualDeliveryDate() == null) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }
        order.setUpdatedBy("wms");
        order.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity saved = purchaseOrderRepository.save(order);
        purchaseOrderEventOutboxService.enqueuePoEvent(saved.getOrderNo(), "PO_RECEIPT_UPDATED");
        List<PoFactRequest.ReceiptLine> factLines = req.getLines().stream()
                .filter(l -> l != null && l.getMaterialCode() != null && !l.getMaterialCode().isBlank() && l.getQty() != null)
                .map(l -> {
                    PoFactRequest.ReceiptLine r = new PoFactRequest.ReceiptLine();
                    r.setMaterialCode(l.getMaterialCode());
                    r.setQty(l.getQty());
                    return r;
                })
                .collect(Collectors.toList());
        erpPoFactOutboxService.enqueueReceiptCompleted(saved.getOrderNo(), req.getReceiptNo(), factLines, saved.getOrderStatus());

        Map<String, Object> data = new HashMap<>();
        data.put("poNo", req.getPoNo());
        data.put("receiptNo", req.getReceiptNo());
        data.put("updatedItems", updatedItems);
        data.put("orderStatus", order.getOrderStatus());
        return success("成功", data);
    }

    @PostMapping("/wms/warehouse-events")
    public ApiResponse<Map<String, Object>> receiveWarehouseEvents(@RequestBody WarehouseEventRequest req) {
        warehouseLocationMirrorService.applyWarehouseEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return success("成功", data);
    }

    @PostMapping("/wms/location-events")
    public ApiResponse<Map<String, Object>> receiveLocationEvents(@RequestBody LocationEventRequest req) {
        warehouseLocationMirrorService.applyLocationEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return success("成功", data);
    }

    @PostMapping("/wms/outbound-shipped")
    public ApiResponse<Map<String, Object>> outboundShipped(@RequestBody OutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null || req.getOutboundOrder().getOrderNo() == null || req.getOutboundOrder().getOrderNo().isBlank()) {
            return badRequest("orderNo不能为空");
        }
        boolean ok = scmOutboundShipmentFactService.applyWmsOutboundShipped(req);
        if (!ok) {
            return ApiResponse.error(409, "事件重复或处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req.getEventKey());
        data.put("eventId", req.getEventId());
        data.put("orderNo", req.getOutboundOrder().getOrderNo());
        return success("成功", data);
    }

    @PostMapping("/qms/iqc-completed")
    public ApiResponse<Map<String, Object>> iqcCompleted(@RequestBody IqcCompletedRequest req) {
        if (req == null || req.getPoNo() == null || req.getPoNo().isBlank()) {
            return badRequest("poNo不能为空");
        }
        if (req.getQcNo() == null || req.getQcNo().isBlank()) {
            return badRequest("qcNo不能为空");
        }
        if (req.getResult() == null || req.getResult().isBlank()) {
            return badRequest("result不能为空");
        }
        PurchaseOrderEntity order = purchaseOrderRepository.findByOrderNo(req.getPoNo()).orElse(null);
        if (order == null) {
            return notFound("采购订单不存在");
        }

        String r = req.getResult().trim().toUpperCase();
        if ("FAIL".equals(r)) {
            order.setOrderStatus(58);
        }
        order.setUpdatedBy("qms");
        order.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity saved = purchaseOrderRepository.save(order);
        purchaseOrderEventOutboxService.enqueuePoEvent(saved.getOrderNo(), "PO_QC_UPDATED");
        erpPoFactOutboxService.enqueueIqcCompleted(saved.getOrderNo(), req.getQcNo(), r, saved.getOrderStatus());

        Map<String, Object> data = new HashMap<>();
        data.put("poNo", req.getPoNo());
        data.put("qcNo", req.getQcNo());
        data.put("result", r);
        data.put("orderStatus", order.getOrderStatus());
        return success("成功", data);
    }

    /**
     * 接收 MES 工单完工事实（B6 闭环入口）。
     *
     * <p>MES 工单完工后通过 Outbox 推送完工事件，SCM 收到后落 scm_production_completion_fact 表，
     * 形成制造回流，闭环生产链。幂等由 {@link MesCompletionFactService#applyMesCompletion} 保证。
     *
     * @param req MES 完工事实 DTO
     * @return 处理结果（含事件键、ERP 生产单号、工单号）
     */
    @PostMapping("/mes/completion")
    public ApiResponse<Map<String, Object>> receiveMesCompletion(@RequestBody MesCompletionFactDTO req) {
        if (req == null || req.getErpProductionNo() == null || req.getErpProductionNo().isBlank()) {
            return badRequest("erpProductionNo不能为空");
        }
        if (req.getWorkOrderNo() == null || req.getWorkOrderNo().isBlank()) {
            return badRequest("workOrderNo不能为空");
        }
        com.hxcoe.scm.entity.ScmProductionCompletionFactEntity fact = mesCompletionFactService.applyMesCompletion(req);
        if (fact == null) {
            return ApiResponse.error(409, "完工事实处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req.getEventKey());
        data.put("eventId", req.getEventId());
        data.put("idempotencyKey", fact.getIdempotencyKey());
        data.put("erpProductionNo", fact.getErpProductionNo());
        data.put("workOrderNo", fact.getWorkOrderNo());
        return success("成功", data);
    }

    public static class ReceiptCompletedRequest {
        private String poNo;
        private String receiptNo;
        private List<ReceiptLine> lines;

        public String getPoNo() {
            return poNo;
        }

        public void setPoNo(String poNo) {
            this.poNo = poNo;
        }

        public String getReceiptNo() {
            return receiptNo;
        }

        public void setReceiptNo(String receiptNo) {
            this.receiptNo = receiptNo;
        }

        public List<ReceiptLine> getLines() {
            return lines;
        }

        public void setLines(List<ReceiptLine> lines) {
            this.lines = lines;
        }
    }

    public static class ReceiptLine {
        private String materialCode;
        private BigDecimal qty;

        public String getMaterialCode() {
            return materialCode;
        }

        public void setMaterialCode(String materialCode) {
            this.materialCode = materialCode;
        }

        public BigDecimal getQty() {
            return qty;
        }

        public void setQty(BigDecimal qty) {
            this.qty = qty;
        }
    }

    public static class IqcCompletedRequest {
        private String poNo;
        private String qcNo;
        private String result;

        public String getPoNo() {
            return poNo;
        }

        public void setPoNo(String poNo) {
            this.poNo = poNo;
        }

        public String getQcNo() {
            return qcNo;
        }

        public void setQcNo(String qcNo) {
            this.qcNo = qcNo;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
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
