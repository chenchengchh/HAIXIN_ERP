package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.PurchaseReconciliationQueryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping({"/api/v1/scm/reports", "/scm/reports"})
public class PurchaseReconciliationReportController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private PurchaseReconciliationQueryService purchaseReconciliationQueryService;

    @GetMapping("/purchase-reconciliation/page")
    public ApiResponse<PageResult<Map<String, Object>>> purchaseReconciliationPage(@RequestParam(defaultValue = "1") Integer page,
                                                                                   @RequestParam(defaultValue = "10") Integer size,
                                                                                   @RequestParam(defaultValue = "false") Boolean onlyMismatch,
                                                                                   @RequestParam(required = false) String orderNo,
                                                                                   @RequestParam(required = false) String supplierCode,
                                                                                   @RequestParam(required = false) String supplierName,
                                                                                   @RequestParam(required = false) String materialCode,
                                                                                   @RequestParam(required = false) String startDate,
                                                                                   @RequestParam(required = false) String endDate) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);

        if (Boolean.TRUE.equals(onlyMismatch)) {
            long total = purchaseReconciliationQueryService.countMismatchOrders(orderNo, supplierCode, supplierName, materialCode, start, end);
            List<String> orderNos = purchaseReconciliationQueryService.findMismatchOrderNos(p, s, orderNo, supplierCode, supplierName, materialCode, start, end);
            if (orderNos.isEmpty()) {
                return success("成功", PageResult.build(total, s, p, List.of()));
            }

            List<PurchaseOrderEntity> ordersRaw = purchaseOrderRepository.findByOrderNoIn(orderNos);
            Map<String, PurchaseOrderEntity> orderMap = new HashMap<>();
            for (PurchaseOrderEntity o : ordersRaw) {
                if (o != null && o.getOrderNo() != null) {
                    orderMap.put(o.getOrderNo(), o);
                }
            }
            List<PurchaseOrderEntity> orders = new ArrayList<>();
            for (String no : orderNos) {
                PurchaseOrderEntity o = orderMap.get(no);
                if (o != null) orders.add(o);
            }

            Map<String, List<PurchaseOrderItemEntity>> itemsByNo = new HashMap<>();
            List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNoIn(orderNos);
            for (PurchaseOrderItemEntity it : items) {
                if (it == null || it.getOrderNo() == null) continue;
                itemsByNo.computeIfAbsent(it.getOrderNo(), k -> new ArrayList<>()).add(it);
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            for (PurchaseOrderEntity o : orders) {
                List<PurchaseOrderItemEntity> its = itemsByNo.getOrDefault(o.getOrderNo(), List.of());
                rows.add(buildRow(o, its));
            }
            return success("成功", PageResult.build(total, s, p, rows));
        }

        Pageable pageable = PageRequest.of(p - 1, s);
        Specification<PurchaseOrderEntity> spec = buildPurchaseOrderSpec(orderNo, supplierCode, supplierName, materialCode, start, end);
        Page<PurchaseOrderEntity> poPage = purchaseOrderRepository.findAll(spec, pageable);
        List<PurchaseOrderEntity> orders = poPage.getContent() == null ? List.of() : poPage.getContent();
        List<String> orderNos = new ArrayList<>();
        for (PurchaseOrderEntity o : orders) {
            if (o != null && o.getOrderNo() != null && !o.getOrderNo().isBlank()) {
                orderNos.add(o.getOrderNo());
            }
        }
        Map<String, List<PurchaseOrderItemEntity>> itemsByNo = new HashMap<>();
        if (!orderNos.isEmpty()) {
            List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNoIn(orderNos);
            for (PurchaseOrderItemEntity it : items) {
                if (it == null || it.getOrderNo() == null) continue;
                itemsByNo.computeIfAbsent(it.getOrderNo(), k -> new ArrayList<>()).add(it);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (PurchaseOrderEntity o : orders) {
            if (o == null) continue;
            List<PurchaseOrderItemEntity> items = itemsByNo.getOrDefault(o.getOrderNo(), List.of());
            rows.add(buildRow(o, items));
        }

        PageResult<Map<String, Object>> pageResult = PageResult.build(
                poPage.getTotalElements(),
                poPage.getSize(),
                poPage.getNumber() + 1,
                rows
        );
        return success("成功", pageResult);
    }

    @GetMapping("/purchase-reconciliation/summary")
    public ApiResponse<Map<String, Object>> purchaseReconciliationSummary(@RequestParam(defaultValue = "1000") Integer limit,
                                                                          @RequestParam(required = false) String orderNo,
                                                                          @RequestParam(required = false) String supplierCode,
                                                                          @RequestParam(required = false) String supplierName,
                                                                          @RequestParam(required = false) String materialCode,
                                                                          @RequestParam(required = false) String startDate,
                                                                          @RequestParam(required = false) String endDate) {
        int l = limit == null || limit <= 0 ? 1000 : Math.min(limit, 5000);
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Pageable pageable = PageRequest.of(0, l);
        Specification<PurchaseOrderEntity> spec = buildPurchaseOrderSpec(orderNo, supplierCode, supplierName, materialCode, start, end);
        Page<PurchaseOrderEntity> poPage = purchaseOrderRepository.findAll(spec, pageable);
        List<PurchaseOrderEntity> orders = poPage.getContent() == null ? List.of() : poPage.getContent();

        List<String> orderNos = new ArrayList<>();
        for (PurchaseOrderEntity o : orders) {
            if (o != null && o.getOrderNo() != null && !o.getOrderNo().isBlank()) {
                orderNos.add(o.getOrderNo());
            }
        }
        Map<String, List<PurchaseOrderItemEntity>> itemsByNo = new HashMap<>();
        if (!orderNos.isEmpty()) {
            List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNoIn(orderNos);
            for (PurchaseOrderItemEntity it : items) {
                if (it == null || it.getOrderNo() == null) continue;
                itemsByNo.computeIfAbsent(it.getOrderNo(), k -> new ArrayList<>()).add(it);
            }
        }

        long total = 0;
        long qcHold = 0;
        long partialReceived = 0;
        long received = 0;
        long mismatch = 0;

        for (PurchaseOrderEntity o : orders) {
            if (o == null) continue;
            total++;
            if (o.getOrderStatus() != null && o.getOrderStatus() == 58) qcHold++;
            if (o.getOrderStatus() != null && o.getOrderStatus() == 50) partialReceived++;
            if (o.getOrderStatus() != null && o.getOrderStatus() == 60) received++;
            Map<String, Object> row = buildRow(o, itemsByNo.getOrDefault(o.getOrderNo(), List.of()));
            if (Boolean.TRUE.equals(row.get("hasMismatch"))) mismatch++;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("scannedLimit", l);
        data.put("totalOrders", total);
        data.put("qcHoldOrders", qcHold);
        data.put("partialReceivedOrders", partialReceived);
        data.put("receivedOrders", received);
        data.put("mismatchOrders", mismatch);
        return success("成功", data);
    }

    @GetMapping("/purchase-reconciliation/export")
    public ResponseEntity<byte[]> exportPurchaseReconciliation(@RequestParam(defaultValue = "false") Boolean onlyMismatch,
                                                               @RequestParam(defaultValue = "5000") Integer limit,
                                                               @RequestParam(required = false) String orderNo,
                                                               @RequestParam(required = false) String supplierCode,
                                                               @RequestParam(required = false) String supplierName,
                                                               @RequestParam(required = false) String materialCode,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate) throws Exception {
        int l = limit == null || limit <= 0 ? 5000 : Math.min(limit, 20000);
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        List<Map<String, Object>> rows;

        if (Boolean.TRUE.equals(onlyMismatch)) {
            rows = loadMismatchRows(orderNo, supplierCode, supplierName, materialCode, start, end, l);
        } else {
            rows = loadAllRows(orderNo, supplierCode, supplierName, materialCode, start, end, l);
        }

        byte[] bytes = buildWorkbookBytes(rows);
        String filename = "purchase-reconciliation-" + LocalDateTime.now().toString().replace(":", "").replace(".", "") + ".xlsx";
        String contentDisposition = "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(bytes);
    }

    @GetMapping("/purchase-reconciliation/reason-summary")
    public ApiResponse<Map<String, Object>> purchaseReconciliationReasonSummary(@RequestParam(required = false) String orderNo,
                                                                                @RequestParam(required = false) String supplierCode,
                                                                                @RequestParam(required = false) String supplierName,
                                                                                @RequestParam(required = false) String materialCode,
                                                                                @RequestParam(required = false) String startDate,
                                                                                @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Map<String, Object> data = purchaseReconciliationQueryService.getReasonSummary(orderNo, supplierCode, supplierName, materialCode, start, end);
        return success("成功", data);
    }

    @GetMapping("/purchase-reconciliation/aggregate/suppliers/page")
    public ApiResponse<PageResult<Map<String, Object>>> purchaseReconciliationSupplierAgg(@RequestParam(defaultValue = "1") Integer page,
                                                                                          @RequestParam(defaultValue = "10") Integer size,
                                                                                          @RequestParam(required = false) String orderNo,
                                                                                          @RequestParam(required = false) String supplierCode,
                                                                                          @RequestParam(required = false) String supplierName,
                                                                                          @RequestParam(required = false) String materialCode,
                                                                                          @RequestParam(required = false) String startDate,
                                                                                          @RequestParam(required = false) String endDate) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        long total = purchaseReconciliationQueryService.countSupplierAgg(orderNo, supplierCode, supplierName, materialCode, start, end);
        List<Map<String, Object>> rows = purchaseReconciliationQueryService.findSupplierAgg(p, s, orderNo, supplierCode, supplierName, materialCode, start, end);
        return success("成功", PageResult.build(total, s, p, rows));
    }

    @GetMapping("/purchase-reconciliation/aggregate/materials/page")
    public ApiResponse<PageResult<Map<String, Object>>> purchaseReconciliationMaterialAgg(@RequestParam(defaultValue = "1") Integer page,
                                                                                          @RequestParam(defaultValue = "10") Integer size,
                                                                                          @RequestParam(required = false) String orderNo,
                                                                                          @RequestParam(required = false) String supplierCode,
                                                                                          @RequestParam(required = false) String supplierName,
                                                                                          @RequestParam(required = false) String materialCode,
                                                                                          @RequestParam(required = false) String startDate,
                                                                                          @RequestParam(required = false) String endDate) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        long total = purchaseReconciliationQueryService.countMaterialAgg(orderNo, supplierCode, supplierName, materialCode, start, end);
        List<Map<String, Object>> rows = purchaseReconciliationQueryService.findMaterialAgg(p, s, orderNo, supplierCode, supplierName, materialCode, start, end);
        return success("成功", PageResult.build(total, s, p, rows));
    }

    @GetMapping("/purchase-orders/qc-hold/page")
    public ApiResponse<PageResult<PurchaseOrderEntity>> qcHoldOrders(@RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(p - 1, s);
        Page<PurchaseOrderEntity> poPage = purchaseOrderRepository.findByOrderStatus(58, pageable);
        PageResult<PurchaseOrderEntity> pageResult = PageResult.build(
                poPage.getTotalElements(),
                poPage.getSize(),
                poPage.getNumber() + 1,
                poPage.getContent()
        );
        return success("成功", pageResult);
    }

    private List<Map<String, Object>> loadMismatchRows(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end, int limit) {
        List<String> orderNos = new ArrayList<>();
        int page = 1;
        int size = Math.min(500, limit);
        while (orderNos.size() < limit) {
            List<String> batch = purchaseReconciliationQueryService.findMismatchOrderNos(page, size, orderNo, supplierCode, supplierName, materialCode, start, end);
            if (batch.isEmpty()) break;
            for (String no : batch) {
                if (orderNos.size() >= limit) break;
                orderNos.add(no);
            }
            page++;
        }
        return buildRowsByOrderNos(orderNos);
    }

    private List<Map<String, Object>> loadAllRows(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end, int limit) {
        List<String> orderNos = new ArrayList<>();
        int page = 1;
        int size = Math.min(500, limit);
        Specification<PurchaseOrderEntity> spec = buildPurchaseOrderSpec(orderNo, supplierCode, supplierName, materialCode, start, end);
        while (orderNos.size() < limit) {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<PurchaseOrderEntity> poPage = purchaseOrderRepository.findAll(spec, pageable);
            List<PurchaseOrderEntity> list = poPage.getContent() == null ? List.of() : poPage.getContent();
            if (list.isEmpty()) break;
            for (PurchaseOrderEntity o : list) {
                if (orderNos.size() >= limit) break;
                if (o != null && o.getOrderNo() != null && !o.getOrderNo().isBlank()) {
                    orderNos.add(o.getOrderNo());
                }
            }
            page++;
        }
        return buildRowsByOrderNos(orderNos);
    }

    private List<Map<String, Object>> buildRowsByOrderNos(List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) return List.of();
        List<PurchaseOrderEntity> ordersRaw = purchaseOrderRepository.findByOrderNoIn(orderNos);
        Map<String, PurchaseOrderEntity> orderMap = new HashMap<>();
        for (PurchaseOrderEntity o : ordersRaw) {
            if (o != null && o.getOrderNo() != null) {
                orderMap.put(o.getOrderNo(), o);
            }
        }
        List<PurchaseOrderEntity> orders = new ArrayList<>();
        for (String no : orderNos) {
            PurchaseOrderEntity o = orderMap.get(no);
            if (o != null) orders.add(o);
        }
        Map<String, List<PurchaseOrderItemEntity>> itemsByNo = new HashMap<>();
        List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNoIn(orderNos);
        for (PurchaseOrderItemEntity it : items) {
            if (it == null || it.getOrderNo() == null) continue;
            itemsByNo.computeIfAbsent(it.getOrderNo(), k -> new ArrayList<>()).add(it);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PurchaseOrderEntity o : orders) {
            rows.add(buildRow(o, itemsByNo.getOrDefault(o.getOrderNo(), List.of())));
        }
        return rows;
    }

    private byte[] buildWorkbookBytes(List<Map<String, Object>> rows) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("purchase-reconciliation");
            int r = 0;
            Row header = sheet.createRow(r++);
            String[] cols = new String[]{
                    "orderNo", "supplierCode", "supplierName", "orderStatus",
                    "headAmount", "itemAmountSum", "amountDiff", "amountMismatch",
                    "orderedQtySum", "receivedQtySum", "receiveRate", "anyOverReceived",
                    "qcHold", "hasMismatch", "itemCount"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
            }
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Row rr = sheet.createRow(r++);
                    for (int i = 0; i < cols.length; i++) {
                        Object v = row == null ? null : row.get(cols[i]);
                        Cell c = rr.createCell(i);
                        if (v == null) {
                            c.setCellValue("");
                        } else if (v instanceof Number n) {
                            c.setCellValue(n.doubleValue());
                        } else if (v instanceof Boolean b) {
                            c.setCellValue(b ? "true" : "false");
                        } else {
                            c.setCellValue(String.valueOf(v));
                        }
                    }
                }
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
                wb.write(bos);
                return bos.toByteArray();
            }
        }
    }

    private Specification<PurchaseOrderEntity> buildPurchaseOrderSpec(String orderNo, String supplierCode, String supplierName, String materialCode, LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo.trim() + "%"));
            }
            if (supplierCode != null && !supplierCode.isBlank()) {
                predicates.add(cb.like(root.get("supplierCode"), "%" + supplierCode.trim() + "%"));
            }
            if (supplierName != null && !supplierName.isBlank()) {
                predicates.add(cb.like(root.get("supplierName"), "%" + supplierName.trim() + "%"));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), end));
            }
            if (materialCode != null && !materialCode.isBlank()) {
                var sq = query.subquery(Long.class);
                var itemRoot = sq.from(PurchaseOrderItemEntity.class);
                sq.select(cb.literal(1L))
                        .where(
                                cb.equal(itemRoot.get("orderNo"), root.get("orderNo")),
                                cb.equal(itemRoot.get("materialCode"), materialCode.trim())
                        );
                predicates.add(cb.exists(sq));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private LocalDateTime parseStart(String s) {
        if (s == null || s.isBlank()) return null;
        LocalDate d = LocalDate.parse(s.trim());
        return d.atStartOfDay();
    }

    private LocalDateTime parseEnd(String s) {
        if (s == null || s.isBlank()) return null;
        LocalDate d = LocalDate.parse(s.trim());
        return d.atTime(23, 59, 59);
    }

    private Map<String, Object> buildRow(PurchaseOrderEntity order, List<PurchaseOrderItemEntity> items) {
        BigDecimal headAmount = order.getOrderAmount() == null ? BigDecimal.ZERO : order.getOrderAmount();
        BigDecimal itemAmountSum = BigDecimal.ZERO;
        BigDecimal orderedQtySum = BigDecimal.ZERO;
        BigDecimal receivedQtySum = BigDecimal.ZERO;
        boolean anyOverReceived = false;

        for (PurchaseOrderItemEntity it : items) {
            if (it == null) continue;
            if (it.getAmount() != null) {
                itemAmountSum = itemAmountSum.add(it.getAmount());
            }
            if (it.getQuantity() != null) {
                orderedQtySum = orderedQtySum.add(it.getQuantity());
            }
            BigDecimal r = it.getReceivedQuantity() == null ? BigDecimal.ZERO : it.getReceivedQuantity();
            receivedQtySum = receivedQtySum.add(r);
            if (it.getQuantity() != null && r.compareTo(it.getQuantity()) > 0) {
                anyOverReceived = true;
            }
        }

        BigDecimal amountDiff = headAmount.subtract(itemAmountSum);
        boolean amountMismatch = amountDiff.abs().compareTo(new BigDecimal("0.01")) > 0;

        BigDecimal receiveRate = BigDecimal.ZERO;
        if (orderedQtySum.compareTo(BigDecimal.ZERO) > 0) {
            receiveRate = receivedQtySum.divide(orderedQtySum, 6, RoundingMode.HALF_UP);
        }

        Map<String, Object> row = new HashMap<>();
        row.put("orderNo", order.getOrderNo());
        row.put("supplierCode", order.getSupplierCode());
        row.put("supplierName", order.getSupplierName());
        row.put("orderStatus", order.getOrderStatus());
        row.put("headAmount", headAmount);
        row.put("itemAmountSum", itemAmountSum);
        row.put("amountDiff", amountDiff);
        row.put("amountMismatch", amountMismatch);
        row.put("orderedQtySum", orderedQtySum);
        row.put("receivedQtySum", receivedQtySum);
        row.put("receiveRate", receiveRate);
        row.put("anyOverReceived", anyOverReceived);
        row.put("qcHold", order.getOrderStatus() != null && order.getOrderStatus() == 58);
        row.put("hasMismatch", amountMismatch || anyOverReceived);
        row.put("itemCount", items.size());
        return row;
    }
}
