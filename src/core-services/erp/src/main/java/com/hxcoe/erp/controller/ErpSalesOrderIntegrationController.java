package com.hxcoe.erp.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.erp.entity.ErpSalesOrderMirrorEntity;
import com.hxcoe.erp.repository.ErpSalesOrderMirrorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ERP销售订单集成控制器（O2C核心链路：接收CRM已确认销售订单）
 */
@RestController
@RequestMapping({"/api/v1/erp/integration", "/erp/integration"})
public class ErpSalesOrderIntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(ErpSalesOrderIntegrationController.class);

    @Autowired
    private ErpSalesOrderMirrorRepository erpSalesOrderMirrorRepository;

    /**
     * 接收CRM已确认销售订单，按 orderNo 幂等 UPSERT 落地镜像，
     * 供后续应收与生产/出库计划使用。
     *
     * @param body 订单事件载荷（eventId/orderNo/customerId/customerName/totalAmount/currency/orderDate/status/items）
     * @return 处理结果，data 携带 orderNo 与 action（created/updated）
     */
    @PostMapping("/crm/sales-order")
    public Result<Map<String, Object>> receiveCrmSalesOrder(@RequestBody Map<String, Object> body) {
        if (body == null) {
            return Result.fail("订单载荷不能为空");
        }
        String orderNo = asString(body.get("orderNo"));
        if (orderNo == null || orderNo.isBlank()) {
            return Result.fail("orderNo不能为空");
        }

        // 幂等处理：按 orderNo 查重，存在则更新，不存在则插入
        Optional<ErpSalesOrderMirrorEntity> existing = erpSalesOrderMirrorRepository.findByOrderNo(orderNo);
        ErpSalesOrderMirrorEntity mirror;
        String action;
        if (existing.isPresent()) {
            mirror = existing.get();
            action = "updated";
        } else {
            mirror = new ErpSalesOrderMirrorEntity();
            mirror.setOrderNo(orderNo);
            mirror.setCreateTime(LocalDateTime.now());
            action = "created";
        }

        mirror.setCustomerId(parseLong(body.get("customerId")));
        mirror.setCustomerName(asString(body.get("customerName")));
        mirror.setTotalAmount(parseBigDecimal(body.get("totalAmount")));
        mirror.setCurrency(asString(body.get("currency")));
        mirror.setOrderDate(parseLocalDate(asString(body.get("orderDate"))));
        mirror.setStatus(asString(body.get("status")));
        mirror.setReceiveTime(LocalDateTime.now());

        erpSalesOrderMirrorRepository.save(mirror);
        logger.info("CRM销售订单落地ERP镜像: eventId={} orderNo={} action={} status={}",
                body.get("eventId"), orderNo, action, mirror.getStatus());

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("action", action);
        return Result.success("成功", data);
    }

    /**
     * 将载荷值安全转换为字符串
     *
     * @param value 原始值
     * @return 字符串，原始值为空时返回 null
     */
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 将载荷值安全转换为 Long（兼容数字与字符串）
     *
     * @param value 原始值
     * @return Long 值，无法解析时返回 null
     */
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将载荷值安全转换为 BigDecimal（兼容数字与字符串）
     *
     * @param value 原始值
     * @return BigDecimal 值，无法解析时返回 null
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将载荷值安全转换为 LocalDate（格式 yyyy-MM-dd）
     *
     * @param value 原始字符串
     * @return LocalDate 值，无法解析时返回 null
     */
    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
