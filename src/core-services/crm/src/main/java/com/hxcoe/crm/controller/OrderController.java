package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.SalesOrderEntity;
import com.hxcoe.crm.entity.SalesOrderItemEntity;
import com.hxcoe.crm.repository.SalesOrderItemRepository;
import com.hxcoe.crm.service.SalesOrderService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/orders")
public class OrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderItemRepository salesOrderItemRepository;

    /**
     * 获取我的订单列表
     */
    @GetMapping("/my")
    public Result<PageResult<SalesOrderEntity>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String customerName) {
        
        Specification<SalesOrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                Predicate p1 = cb.like(root.get("orderNo"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("customerName"), "%" + keyword + "%");
                predicates.add(cb.or(p1, p2));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerName != null && !customerName.isEmpty()) {
                predicates.add(cb.like(root.get("customerName"), "%" + customerName + "%"));
            }
            if (startDate != null && !startDate.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), LocalDate.parse(startDate)));
            }
            if (endDate != null && !endDate.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), LocalDate.parse(endDate)));
            }
            // 模拟"我的订单"：假设当前用户ID为1（salesPersonId）
            // predicates.add(cb.equal(root.get("salesPersonId"), 1L));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SalesOrderEntity> result = salesOrderService.getOrderList(spec, pageable);
        
        return Result.success(PageResult.build(result.getTotalElements(), result.getSize(), page, result.getContent()));
    }

    /**
     * 创建销售订单
     */
    @PostMapping
    public Result<SalesOrderEntity> createOrder(@RequestBody SalesOrderEntity order) {
        SalesOrderEntity created = salesOrderService.createOrder(order);
        return Result.success("订单创建成功", created);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<SalesOrderEntity> getOrderById(@PathVariable Long id) {
        Optional<SalesOrderEntity> order = salesOrderService.getOrderById(id);
        return order.map(Result::success).orElseGet(() -> Result.fail("订单不存在"));
    }

    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public Result<SalesOrderEntity> updateOrder(@PathVariable Long id, @RequestBody SalesOrderEntity order) {
        SalesOrderEntity updated = salesOrderService.updateOrder(id, order);
        if (updated != null) {
            return Result.success("订单更新成功", updated);
        } else {
            return Result.fail("订单不存在");
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        salesOrderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }

    /**
     * 分页查询订单明细列表
     * 明细的salesOrder为@JsonBackReference序列化时被忽略，映射为扁平Map携带orderId
     *
     * @param page    页码，默认1
     * @param size    每页大小，默认10
     * @param keyword 产品名称/编码关键词（可选）
     * @param orderId 订单ID过滤（可选）
     * @return 明细分页数据
     */
    @GetMapping("/items")
    @Transactional(readOnly = true)
    public Result<PageResult<Map<String, Object>>> getOrderItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orderId) {

        Specification<SalesOrderItemEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                Predicate p1 = cb.like(root.get("productName"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("productCode"), "%" + keyword + "%");
                predicates.add(cb.or(p1, p2));
            }
            if (orderId != null) {
                predicates.add(cb.equal(root.get("salesOrder").get("id"), orderId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<SalesOrderItemEntity> itemPage = salesOrderItemRepository.findAll(spec, pageable);

        // 映射为扁平结构，补充orderId，避免@JsonBackReference丢失关联信息
        List<Map<String, Object>> items = itemPage.getContent().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("orderId", item.getSalesOrder() != null ? item.getSalesOrder().getId() : null);
            map.put("orderNo", item.getSalesOrder() != null ? item.getSalesOrder().getOrderNo() : null);
            map.put("productId", item.getProductId());
            map.put("productCode", item.getProductCode());
            map.put("productName", item.getProductName());
            map.put("quantity", item.getQuantity());
            map.put("unit", item.getUnit());
            map.put("unitPrice", item.getUnitPrice());
            map.put("discountRate", item.getDiscountRate());
            map.put("subtotal", item.getSubtotal());
            map.put("remark", item.getRemark());
            return map;
        }).collect(Collectors.toList());

        return Result.success(PageResult.build(
                itemPage.getTotalElements(),
                itemPage.getSize(),
                itemPage.getNumber() + 1,
                items
        ));
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit")
    public Result<SalesOrderEntity> submitOrder(@PathVariable Long id) {
        SalesOrderEntity result = salesOrderService.submitOrder(id);
        if (result != null) {
            return Result.success("订单已提交审批", result);
        } else {
            return Result.fail("订单状态不正确或不存在");
        }
    }
}
