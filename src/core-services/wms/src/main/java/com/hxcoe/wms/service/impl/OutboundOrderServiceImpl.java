package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.service.CrmOutboundShipmentSyncService;
import com.hxcoe.wms.service.ErpOutboundShipmentSyncService;
import com.hxcoe.wms.service.OutboundOrderService;
import com.hxcoe.wms.service.ScmOutboundShipmentSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Optional;

@Service
public class OutboundOrderServiceImpl implements OutboundOrderService {

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired(required = false)
    private ErpOutboundShipmentSyncService erpOutboundShipmentSyncService;

    @Autowired(required = false)
    private ScmOutboundShipmentSyncService scmOutboundShipmentSyncService;

    @Autowired(required = false)
    private CrmOutboundShipmentSyncService crmOutboundShipmentSyncService;

    @Transactional
    @Override
    public OutboundOrderEntity createOutboundOrder(OutboundOrderEntity order) {
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo("OUT-" + System.currentTimeMillis());
        }
        if (order.getItems() != null) {
            for (OutboundOrderItemEntity item : order.getItems()) {
                item.setOutboundOrder(order);
            }
        }
        order.setStatus("CREATED");
        return outboundOrderRepository.save(order);
    }

    @Override
    public Page<OutboundOrderEntity> getOutboundOrders(Pageable pageable, String status, String type, String orderNo, String customerName, Long waveId) {
        Specification<OutboundOrderEntity> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type.trim()));
            }
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo.trim() + "%"));
            }
            if (customerName != null && !customerName.isBlank()) {
                predicates.add(cb.like(root.get("customerName"), "%" + customerName.trim() + "%"));
            }
            if (waveId != null) {
                predicates.add(cb.equal(root.get("waveId"), waveId));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return outboundOrderRepository.findAll(spec, pageable);
    }

    /**
     * 更新出库单：仅CREATED状态允许编辑，更新头字段并整体替换明细
     */
    @Transactional
    @Override
    public OutboundOrderEntity updateOutboundOrder(Long id, OutboundOrderEntity order) {
        Optional<OutboundOrderEntity> optional = outboundOrderRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        OutboundOrderEntity existing = optional.get();
        if (!"CREATED".equalsIgnoreCase(existing.getStatus() == null ? "" : existing.getStatus().trim())) {
            throw new IllegalStateException("仅新建状态的出库单允许编辑");
        }
        existing.setType(order.getType());
        existing.setSourceNo(order.getSourceNo());
        existing.setCustomerName(order.getCustomerName());
        existing.setAddress(order.getAddress());
        existing.setRemark(order.getRemark());
        // 整体替换明细（orphanRemoval自动清理旧明细）
        existing.getItems().clear();
        if (order.getItems() != null) {
            for (OutboundOrderItemEntity item : order.getItems()) {
                item.setId(null);
                item.setOutboundOrder(existing);
                existing.getItems().add(item);
            }
        }
        return outboundOrderRepository.save(existing);
    }

    @Override
    public Optional<OutboundOrderEntity> getOutboundOrderById(Long id) {
        return outboundOrderRepository.findById(id);
    }

    @Transactional
    @Override
    public OutboundOrderEntity shipOutboundOrder(Long id) {
        Optional<OutboundOrderEntity> optional = outboundOrderRepository.findById(id);
        if (optional.isPresent()) {
            OutboundOrderEntity order = optional.get();
            
            // 简单的库存扣减逻辑
            if (order.getItems() != null) {
                for (OutboundOrderItemEntity item : order.getItems()) {
                    // 查找该物料的库存记录
                    java.util.List<com.hxcoe.wms.entity.InventoryEntity> inventories = inventoryRepository.findByMaterialCode(item.getMaterialCode());
                    
                    java.math.BigDecimal remainingQtyToDeduct = item.getQuantity();
                    
                    for (com.hxcoe.wms.entity.InventoryEntity inv : inventories) {
                        if (remainingQtyToDeduct.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                            break;
                        }
                        
                        if (inv.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            java.math.BigDecimal deductQty = inv.getQuantity().min(remainingQtyToDeduct);
                            inv.setQuantity(inv.getQuantity().subtract(deductQty));
                            inventoryRepository.save(inv);
                            remainingQtyToDeduct = remainingQtyToDeduct.subtract(deductQty);

                            InventoryTransactionEntity tx = new InventoryTransactionEntity();
                            tx.setType("OUT");
                            tx.setWarehouseCode(inv.getWarehouseCode());
                            tx.setLocationCode(inv.getLocationCode());
                            tx.setMaterialCode(inv.getMaterialCode());
                            tx.setMaterialName(inv.getMaterialName());
                            tx.setQuantity(deductQty);
                            tx.setUnit(inv.getUnit());
                            tx.setBatchNo(inv.getBatchNo());
                            tx.setSourceNo(order.getOrderNo());
                            tx.setOperator("SYSTEM");
                            inventoryTransactionRepository.save(tx);
                        }
                    }

                    if (remainingQtyToDeduct.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        throw new RuntimeException("库存不足: " + item.getMaterialCode());
                    }
                }
            }
            
            order.setStatus("SHIPPED");
            OutboundOrderEntity saved = outboundOrderRepository.save(order);
            if (erpOutboundShipmentSyncService != null) {
                erpOutboundShipmentSyncService.enqueueOutboundShippedIfAbsent(saved);
            }
            if (scmOutboundShipmentSyncService != null) {
                scmOutboundShipmentSyncService.enqueueOutboundShippedIfAbsent(saved);
            }
            if (crmOutboundShipmentSyncService != null) {
                crmOutboundShipmentSyncService.enqueueOutboundShippedIfAbsent(saved);
            }
            return saved;
        }
        return null;
    }

    @Override
    public OutboundOrderEntity approveOutboundOrder(Long id) {
        Optional<OutboundOrderEntity> optional = outboundOrderRepository.findById(id);
        if (optional.isPresent()) {
            OutboundOrderEntity order = optional.get();
            order.setStatus("APPROVED");
            return outboundOrderRepository.save(order);
        }
        return null;
    }

    @Override
    public OutboundOrderEntity cancelOutboundOrder(Long id) {
        Optional<OutboundOrderEntity> optional = outboundOrderRepository.findById(id);
        if (optional.isPresent()) {
            OutboundOrderEntity order = optional.get();
            order.setStatus("CANCELLED");
            return outboundOrderRepository.save(order);
        }
        return null;
    }
}
