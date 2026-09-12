package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/supplier-collaboration", "/api/scm/supplier-collaboration"})
public class SupplierCollaborationController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/purchase-orders")
    public ApiResponse<Map<String, Object>> getPurchaseOrders(@RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(p - 1, s);
        Result<PageResult<PurchaseOrderEntity>> res = purchaseOrderService.getOrdersByPage(pageable);
        if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode()) || res.getData() == null) {
            return success("成功", Map.of("total", 0, "page", p, "size", s, "list", List.of()));
        }
        PageResult<PurchaseOrderEntity> pageResult = res.getData();
        long total = pageResult.getTotal();
        List<Map<String, Object>> list = new ArrayList<>();
        for (PurchaseOrderEntity r : pageResult.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("orderId", r.getOrderNo() != null ? r.getOrderNo() : r.getId());
            row.put("supplierName", r.getSupplierName());
            row.put("orderDate", toDateString(r.getCreatedTime()));
            row.put("deliveryDate", toDateString(r.getExpectedDeliveryDate()));
            row.put("status", r.getOrderStatus());
            list.add(row);
        }
        return success("成功", Map.of("total", total, "page", p, "size", s, "list", list));
    }

    private String toDateString(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}
