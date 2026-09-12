package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.client.ErpSalesOrderClient;
import com.hxcoe.crm.client.WmsClient;
import com.hxcoe.crm.client.dto.WmsOutboundOrderDTO;
import com.hxcoe.crm.client.dto.WmsOutboundOrderItemDTO;
import com.hxcoe.crm.entity.SalesOrderEntity;
import com.hxcoe.crm.entity.SalesOrderItemEntity;
import com.hxcoe.crm.repository.SalesOrderRepository;
import com.hxcoe.crm.service.CrmApprovalIntegrationService;
import com.hxcoe.crm.service.SalesOrderService;
import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Logger logger = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

    @Autowired
    private SalesOrderRepository salesOrderRepository;
    
    @Autowired
    private WmsClient wmsClient;

    @Autowired(required = false)
    private ErpSalesOrderClient erpSalesOrderClient;

    @Autowired
    private CrmApprovalIntegrationService crmApprovalIntegrationService;

    @Transactional
    @Override
    public SalesOrderEntity createOrder(SalesOrderEntity order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("draft");
        }
        
        // 关联明细并计算金额
        if (order.getItems() != null) {
            for (SalesOrderItemEntity item : order.getItems()) {
                item.setSalesOrder(order);
            }
        }
        computeAmounts(order);

        return salesOrderRepository.save(order);
    }

    @Transactional
    @Override
    public SalesOrderEntity updateOrder(Long id, SalesOrderEntity order) {
        Optional<SalesOrderEntity> existing = salesOrderRepository.findById(id);
        if (existing.isPresent()) {
            SalesOrderEntity updated = existing.get();
            // 更新字段
            updated.setCustomerId(order.getCustomerId());
            updated.setCustomerName(order.getCustomerName());
            updated.setOpportunityId(order.getOpportunityId());
            updated.setTotalAmount(order.getTotalAmount());
            updated.setDiscountAmount(order.getDiscountAmount());
            updated.setFinalAmount(order.getFinalAmount());
            updated.setCurrency(order.getCurrency());
            updated.setOrderDate(order.getOrderDate());
            updated.setDeliveryDate(order.getDeliveryDate());
            updated.setPaymentTerms(order.getPaymentTerms());
            updated.setDeliveryAddress(order.getDeliveryAddress());
            updated.setSalesPersonId(order.getSalesPersonId());
            updated.setRemark(order.getRemark());
            updated.setUpdateTime(LocalDateTime.now());
            
            // 更新明细（简化：先清空后添加）
            if (order.getItems() != null) {
                updated.getItems().clear();
                for (SalesOrderItemEntity item : order.getItems()) {
                    item.setSalesOrder(updated);
                    updated.getItems().add(item);
                }
            }
            computeAmounts(updated);

            return salesOrderRepository.save(updated);
        }
        return null;
    }

    @Transactional
    @Override
    public void deleteOrder(Long id) {
        salesOrderRepository.deleteById(id);
    }

    /**
     * 计算订单明细小计与订单金额
     * 明细小计 = 数量 × 单价 × (1 - 折扣率/100)，仅在小计为空且数量/单价齐全时计算；
     * 订单总额 = 明细小计之和（总额为空时回填），最终金额 = 总额 - 优惠金额（最终金额为空时回填）
     * @param order 订单实体（含明细）
     */
    private void computeAmounts(SalesOrderEntity order) {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasItems = order.getItems() != null && !order.getItems().isEmpty();
        if (hasItems) {
            for (SalesOrderItemEntity item : order.getItems()) {
                if (item.getSubtotal() == null && item.getQuantity() != null && item.getUnitPrice() != null) {
                    BigDecimal factor = BigDecimal.ONE;
                    if (item.getDiscountRate() != null) {
                        factor = BigDecimal.ONE.subtract(
                                item.getDiscountRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    }
                    item.setSubtotal(item.getQuantity().multiply(item.getUnitPrice())
                            .multiply(factor).setScale(2, RoundingMode.HALF_UP));
                }
                if (item.getSubtotal() != null) {
                    total = total.add(item.getSubtotal());
                }
            }
        }
        if (order.getTotalAmount() == null && hasItems) {
            order.setTotalAmount(total);
        }
        if (order.getFinalAmount() == null && order.getTotalAmount() != null) {
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            order.setFinalAmount(order.getTotalAmount().subtract(discount));
        }
    }

    @Override
    public Optional<SalesOrderEntity> getOrderById(Long id) {
        return salesOrderRepository.findById(id);
    }

    @Override
    public Page<SalesOrderEntity> getOrderList(Specification<SalesOrderEntity> spec, Pageable pageable) {
        return salesOrderRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    public SalesOrderEntity submitOrder(Long id) {
        Optional<SalesOrderEntity> existing = salesOrderRepository.findById(id);
        if (existing.isPresent()) {
            SalesOrderEntity order = existing.get();
            if ("draft".equals(order.getStatus())) {
                order.setStatus("submitted");
                order.setApprovalStatus("pending");
                order.setUpdateTime(LocalDateTime.now());
                SalesOrderEntity saved = salesOrderRepository.save(order);

                // 同步提交OA统一审批（CRM_SALES_ORDER_APPROVAL模板），审批结果经回调驱动最终状态；
                // OA不可用时仅记录日志，不阻断本地提交
                crmApprovalIntegrationService.submitSalesOrderApproval(
                        saved.getId(), saved.getOrderNo(), saved.getCustomerName(),
                        saved.getFinalAmount(), saved.getSalesPersonId(), null);

                return saved;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public SalesOrderEntity rejectOrder(Long id, String comment) {
        Optional<SalesOrderEntity> existing = salesOrderRepository.findById(id);
        if (existing.isPresent()) {
            SalesOrderEntity order = existing.get();
            // 仅审批中（submitted）状态的订单允许被拒绝，防止覆盖已生效状态
            if ("submitted".equals(order.getStatus())) {
                order.setStatus("rejected");
                order.setApprovalStatus("rejected");
                order.setRemark("审批拒绝：" + (comment != null ? comment : ""));
                order.setUpdateTime(LocalDateTime.now());
                return salesOrderRepository.save(order);
            }
        }
        return null;
    }

    @Transactional
    @Override
    public SalesOrderEntity approveOrder(Long id) {
        Optional<SalesOrderEntity> existing = salesOrderRepository.findById(id);
        if (existing.isPresent()) {
            SalesOrderEntity order = existing.get();
            if ("submitted".equals(order.getStatus())) {
                order.setStatus("approved");
                order.setApprovalStatus("approved");
                order.setUpdateTime(LocalDateTime.now());

                // 调用WMS创建出库单
                try {
                    createWmsOutboundOrder(order);
                } catch (Exception e) {
                    logger.error("创建WMS出库单失败: {}", e.getMessage(), e);
                    // 业务决策：是否回滚？暂时仅记录日志，不影响订单审批通过，或者抛出异常回滚
                    // throw new RuntimeException("创建WMS出库单失败");
                }

                SalesOrderEntity saved = salesOrderRepository.save(order);

                // 订单确认（审批通过）后推送ERP，打通O2C主链路（应收与生产/出库计划）
                pushSalesOrderToErp(saved);

                return saved;
            }
        }
        return null;
    }

    /**
     * 推送已确认销售订单到ERP（O2C核心链路）。
     * 推送失败仅记录 warn 日志，不影响订单状态变更。
     *
     * @param order 已确认（审批通过）的销售订单
     */
    private void pushSalesOrderToErp(SalesOrderEntity order) {
        if (erpSalesOrderClient == null) {
            logger.warn("ErpSalesOrderClient未注入，跳过推送ERP: orderNo={}", order.getOrderNo());
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("orderNo", order.getOrderNo());
            body.put("customerId", order.getCustomerId());
            body.put("customerName", order.getCustomerName());
            // 金额优先取最终金额，为空则回退订单总额
            body.put("totalAmount", order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount());
            body.put("currency", order.getCurrency() == null ? "CNY" : order.getCurrency());
            body.put("orderDate", order.getOrderDate() == null ? null : order.getOrderDate().toString());
            // 对ERP统一暴露"已确认"语义状态
            body.put("status", "confirmed");

            List<Map<String, Object>> items = new ArrayList<>();
            if (order.getItems() != null) {
                for (SalesOrderItemEntity item : order.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productCode", item.getProductCode());
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    items.add(itemMap);
                }
            }
            body.put("items", items);

            Result<Map<String, Object>> result = erpSalesOrderClient.pushSalesOrder(body);
            if (result != null && result.getCode() != null && result.getCode() == 200) {
                logger.info("销售订单推送ERP成功: orderNo={}", order.getOrderNo());
            } else {
                logger.warn("销售订单推送ERP返回异常: orderNo={}, msg={}",
                        order.getOrderNo(), result == null ? null : result.getMessage());
            }
        } catch (Exception e) {
            logger.warn("销售订单推送ERP失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
    }
    
    private void createWmsOutboundOrder(SalesOrderEntity order) {
        WmsOutboundOrderDTO wmsOrder = new WmsOutboundOrderDTO();
        wmsOrder.setSourceNo(order.getOrderNo());
        wmsOrder.setOrderType("SALES");
        wmsOrder.setCustomerId(order.getCustomerId());
        wmsOrder.setCustomerName(order.getCustomerName());
        wmsOrder.setRemark(order.getRemark());
        
        List<WmsOutboundOrderItemDTO> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (SalesOrderItemEntity item : order.getItems()) {
                WmsOutboundOrderItemDTO wmsItem = new WmsOutboundOrderItemDTO();
                wmsItem.setMaterialCode(item.getProductCode());
                wmsItem.setMaterialName(item.getProductName());
                wmsItem.setSpecification(null); // CRM可能没有规格字段，暂空
                wmsItem.setPlanQuantity(item.getQuantity());
                wmsItem.setUnit(item.getUnit());
                items.add(wmsItem);
            }
        }
        wmsOrder.setItems(items);
        
        logger.info("正在创建WMS出库单: {}", wmsOrder);
        Result<Object> result = wmsClient.createOutboundOrder(wmsOrder);
        if (result.getCode() == 200) {
            logger.info("WMS出库单创建成功");
        } else {
            logger.error("WMS出库单创建失败: {}", result.getMessage());
            throw new RuntimeException("WMS出库单创建失败: " + result.getMessage());
        }
    }
    
    private String generateOrderNo() {
        return "SO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
