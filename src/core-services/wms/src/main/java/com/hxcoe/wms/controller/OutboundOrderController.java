package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.service.OutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/wms/outbound/orders", "/wms/outbound/orders", "/api/wms/outbound/orders"})
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @PostMapping
    public ApiResponse<OutboundOrderEntity> createOutboundOrder(@RequestBody OutboundOrderEntity order) {
        return success("出库单创建成功", outboundOrderService.createOutboundOrder(order));
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> getOutboundOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "outboundNo", required = false) String outboundNo,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "waveId", required = false) Long waveId
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        String backendType = orderType == null ? null : fromFrontOrderType(orderType.trim());
        Page<OutboundOrderEntity> result = outboundOrderService.getOutboundOrders(pageable, status, backendType, outboundNo, customerName, waveId);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows);
        return success("出库单列表查询成功", pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboundOrderEntity> getOutboundOrderById(@PathVariable("id") Long id) {
        Optional<OutboundOrderEntity> order = outboundOrderService.getOutboundOrderById(id);
        return order.map(entity -> success("出库单查询成功", entity)).orElseGet(() -> notFound("订单不存在"));
    }

    /**
     * 更新出库单（仅CREATED状态可编辑，前端字段通过JsonAlias映射）
     */
    @PutMapping("/{id}")
    public ApiResponse<OutboundOrderEntity> updateOutboundOrder(@PathVariable("id") Long id, @RequestBody OutboundOrderEntity order) {
        try {
            OutboundOrderEntity result = outboundOrderService.updateOutboundOrder(id, order);
            if (result != null) {
                return success("出库单更新成功", result);
            }
            return notFound("出库单不存在");
        } catch (IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<OutboundOrderEntity> shipOutboundOrder(@PathVariable("id") Long id) {
        try {
            OutboundOrderEntity result = outboundOrderService.shipOutboundOrder(id);
            if (result != null) {
                return success("出库单发货成功", result);
            }
            return badRequest("发货失败");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<OutboundOrderEntity> approveOutboundOrder(@PathVariable("id") Long id) {
        OutboundOrderEntity result = outboundOrderService.approveOutboundOrder(id);
        if (result != null) {
            return success("出库单审核成功", result);
        }
        return badRequest("审核失败");
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OutboundOrderEntity> cancelOutboundOrder(@PathVariable("id") Long id) {
        OutboundOrderEntity result = outboundOrderService.cancelOutboundOrder(id);
        if (result != null) {
            return success("出库单取消成功", result);
        }
        return badRequest("取消失败");
    }

    private Map<String, Object> toRow(OutboundOrderEntity order) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", order.getId());
        row.put("outboundNo", order.getOrderNo());
        row.put("orderNo", order.getOrderNo());
        row.put("orderType", toFrontOrderType(order.getType()));
        row.put("sourceNo", order.getSourceNo());
        row.put("customerName", order.getCustomerName());
        row.put("address", order.getAddress());
        row.put("status", order.getStatus());
        row.put("remark", order.getRemark());
        row.put("waveId", order.getWaveId());
        row.put("createTime", formatDateTime(order.getCreatedTime()));
        row.put("updateTime", formatDateTime(order.getUpdatedTime()));
        return row;
    }

    private static String toFrontOrderType(String type) {
        if (type == null) {
            return "";
        }
        String t = type.trim().toUpperCase();
        if ("SALES".equals(t)) {
            return "sales";
        }
        if ("PRODUCTION".equals(t) || "MATERIAL".equals(t)) {
            return "material";
        }
        if ("TRANSFER".equals(t)) {
            return "transfer";
        }
        return type;
    }

    private static String fromFrontOrderType(String type) {
        if (type == null) {
            return "";
        }
        String t = type.trim().toLowerCase();
        if ("sales".equals(t)) {
            return "SALES";
        }
        if ("material".equals(t)) {
            return "PRODUCTION";
        }
        if ("transfer".equals(t)) {
            return "TRANSFER";
        }
        return type;
    }

    private static String formatDateTime(LocalDateTime t) {
        // 统一输出"yyyy-MM-dd HH:mm:ss"，避免toString()微秒非0时格式不一致
        return com.hxcoe.wms.util.WmsDateTimes.formatOrNull(t);
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
