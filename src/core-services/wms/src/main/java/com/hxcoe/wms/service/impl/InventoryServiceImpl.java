package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    private static final BigDecimal LOW_STOCK_THRESHOLD = new BigDecimal("10");
    private static final BigDecimal OVER_STOCK_THRESHOLD = new BigDecimal("1000");

    @Override
    public Page<InventoryEntity> getInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    @Override
    public Page<InventoryEntity> queryInventory(String warehouseCode, String locationCode, String materialCode, String materialName, String status, Pageable pageable) {
        return queryInventory(warehouseCode, locationCode, materialCode, materialName, null, status, pageable);
    }

    @Override
    public Page<InventoryEntity> queryInventory(String warehouseCode, String locationCode, String materialCode, String materialName, String batchNo, String status, Pageable pageable) {
        Specification<InventoryEntity> specification = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (locationCode != null && !locationCode.isBlank()) {
                predicates.add(cb.like(root.get("locationCode"), "%" + locationCode.trim() + "%"));
            }
            if (materialCode != null && !materialCode.isBlank()) {
                predicates.add(cb.like(root.get("materialCode"), "%" + materialCode.trim() + "%"));
            }
            if (materialName != null && !materialName.isBlank()) {
                predicates.add(cb.like(root.get("materialName"), "%" + materialName.trim() + "%"));
            }
            if (batchNo != null && !batchNo.isBlank()) {
                predicates.add(cb.like(root.get("batchNo"), "%" + batchNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                String s = status.trim();
                if ("lowstock".equalsIgnoreCase(s)) {
                    predicates.add(cb.lessThan(root.get("quantity"), LOW_STOCK_THRESHOLD));
                } else if ("overstock".equalsIgnoreCase(s)) {
                    predicates.add(cb.greaterThan(root.get("quantity"), OVER_STOCK_THRESHOLD));
                } else if ("normal".equalsIgnoreCase(s)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), LOW_STOCK_THRESHOLD));
                    predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), OVER_STOCK_THRESHOLD));
                } else if ("expired".equalsIgnoreCase(s)) {
                    predicates.add(cb.disjunction());
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return inventoryRepository.findAll(specification, pageable);
    }

    @Override
    public List<InventoryEntity> getInventoryByMaterialCode(String materialCode) {
        return inventoryRepository.findByMaterialCode(materialCode);
    }

    @Override
    @Transactional
    public void addInventory(InventoryEntity inventory) {
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reduceInventory(Long inventoryId, BigDecimal amount) {
        inventoryRepository.findById(inventoryId).ifPresent(inventory -> {
            BigDecimal newQuantity = inventory.getQuantity().subtract(amount);
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("库存不足");
            }
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
        });
    }

    @Override
    public List<InventoryEntity> getInventoryByLocationCode(String locationCode) {
        return inventoryRepository.findByLocationCode(locationCode);
    }

    @Override
    public List<InventoryEntity> getAlertList(String alertType) {
        // 根据预警类型返回库存预警列表
        if ("lowstock".equalsIgnoreCase(alertType)) {
            return inventoryRepository.findByQuantityLessThan(LOW_STOCK_THRESHOLD);
        } else if ("overstock".equalsIgnoreCase(alertType)) {
            return inventoryRepository.findByQuantityGreaterThan(OVER_STOCK_THRESHOLD);
        }
        return inventoryRepository.findAll();
    }

    @Override
    public InventoryEntity getById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void freezeInventory(Long inventoryId, String reason) {
        inventoryRepository.findById(inventoryId).ifPresent(inventory -> {
            inventory.setStatus("FROZEN");
            inventory.setRemark(reason);
            inventoryRepository.save(inventory);
        });
    }

    @Override
    @Transactional
    public void unfreezeInventory(Long inventoryId) {
        inventoryRepository.findById(inventoryId).ifPresent(inventory -> {
            inventory.setStatus("NORMAL");
            inventory.setRemark(null);
            inventoryRepository.save(inventory);
        });
    }

    @Override
    @Transactional
    public void adjustInventory(Long inventoryId, BigDecimal newQuantity, String reason) {
        inventoryRepository.findById(inventoryId).ifPresent(inventory -> {
            inventory.setQuantity(newQuantity);
            inventory.setRemark(reason);
            inventoryRepository.save(inventory);
        });
    }

    @Override
    @Transactional
    public void checkInventory(Long inventoryId, BigDecimal actualQuantity) {
        inventoryRepository.findById(inventoryId).ifPresent(inventory -> {
            inventory.setQuantity(actualQuantity);
            inventory.setStatus("CHECKED");
            inventoryRepository.save(inventory);
        });
    }
}
