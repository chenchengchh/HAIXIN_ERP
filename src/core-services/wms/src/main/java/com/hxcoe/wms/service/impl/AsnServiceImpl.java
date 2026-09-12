package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.entity.AsnItemEntity;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.repository.AsnRepository;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.service.AsnService;
import com.hxcoe.wms.event.AsnReceivedEvent;
import com.hxcoe.wms.service.ScmReceiptSyncService;
import com.hxcoe.wms.client.ErpClient;
import com.hxcoe.common.dto.erp.IntegrationVoucherDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsnServiceImpl implements AsnService {

    @Autowired
    private AsnRepository asnRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired(required = false)
    private ErpClient erpClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ScmReceiptSyncService scmReceiptSyncService;

    @Autowired
    private com.hxcoe.wms.service.QmsReceiptTriggerSyncService qmsReceiptTriggerSyncService;

    @Transactional
    @Override
    public AsnEntity createAsn(AsnEntity asn) {
        if (asn.getItems() != null) {
            for (AsnItemEntity item : asn.getItems()) {
                item.setAsn(asn);
                if (item.getReceivedQuantity() == null) {
                    item.setReceivedQuantity(BigDecimal.ZERO);
                }
            }
        }
        if (asn.getAsnNo() == null || asn.getAsnNo().isBlank()) {
            asn.setAsnNo("ASN-" + System.currentTimeMillis());
        }
        asn.setStatus("CREATED");
        return asnRepository.save(asn);
    }

    @Transactional
    @Override
    public AsnEntity updateAsn(Long id, AsnEntity asn) {
        Optional<AsnEntity> optional = asnRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        AsnEntity existed = optional.get();
        asn.setId(existed.getId());
        if (asn.getItems() != null) {
            for (AsnItemEntity item : asn.getItems()) {
                item.setAsn(asn);
                if (item.getReceivedQuantity() == null) {
                    item.setReceivedQuantity(BigDecimal.ZERO);
                }
            }
        }
        if (asn.getAsnNo() == null || asn.getAsnNo().isBlank()) {
            asn.setAsnNo(existed.getAsnNo());
        }
        if (asn.getStatus() == null || asn.getStatus().isBlank()) {
            asn.setStatus(existed.getStatus());
        }
        if (asn.getCreatedTime() == null) {
            asn.setCreatedTime(existed.getCreatedTime());
        }
        return asnRepository.save(asn);
    }

    @Transactional
    @Override
    public boolean deleteAsn(Long id) {
        if (id == null) {
            return false;
        }
        if (!asnRepository.existsById(id)) {
            return false;
        }
        asnRepository.deleteById(id);
        return true;
    }

    @Override
    public Page<AsnEntity> getAsns(Pageable pageable) {
        return asnRepository.findAll(pageable);
    }

    @Override
    public Page<AsnEntity> getAsns(Pageable pageable, String asnNo, String supplierName, String status, String startTime, String endTime) {
        // 委托给扩展筛选方法（不支持仓库与送货单号筛选的旧接口）
        return getAsns(pageable, asnNo, null, supplierName, null, status, startTime, endTime);
    }

    @Override
    public Page<AsnEntity> getAsns(Pageable pageable, String asnNo, String deliveryNoteNo, String supplierName, String warehouseCode, String status, String startTime, String endTime) {
        // 使用List<Predicate> + cb.and()模式构建动态查询条件（Hibernate 6兼容写法）
        Specification<AsnEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (asnNo != null && !asnNo.isBlank()) {
                predicates.add(cb.like(root.get("asnNo"), "%" + asnNo.trim() + "%"));
            }
            if (deliveryNoteNo != null && !deliveryNoteNo.isBlank()) {
                predicates.add(cb.like(root.get("deliveryNoteNo"), "%" + deliveryNoteNo.trim() + "%"));
            }
            if (supplierName != null && !supplierName.isBlank()) {
                predicates.add(cb.like(root.get("supplierName"), "%" + supplierName.trim() + "%"));
            }
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (status != null && !status.isBlank()) {
                // 支持逗号分隔多值（如"RECEIVING,PARTIAL_RECEIVED"），用于收货作业UI状态映射多个后端状态
                String[] parts = status.split(",");
                if (parts.length > 1) {
                    List<String> statuses = new ArrayList<>();
                    for (String p : parts) {
                        if (p != null && !p.isBlank()) {
                            statuses.add(p.trim());
                        }
                    }
                    if (!statuses.isEmpty()) {
                        predicates.add(root.get("status").in(statuses));
                    }
                } else {
                    predicates.add(cb.equal(root.get("status"), status.trim()));
                }
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (startTime != null && !startTime.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expectedArrivalDate"), LocalDateTime.parse(startTime.trim(), fmt)));
            }
            if (endTime != null && !endTime.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expectedArrivalDate"), LocalDateTime.parse(endTime.trim(), fmt)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return asnRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<AsnEntity> getAsnById(Long id) {
        return asnRepository.findById(id);
    }

    @Transactional
    @Override
    public AsnEntity receiveAsn(Long id) {
        Optional<AsnEntity> optional = asnRepository.findById(id);
        if (optional.isPresent()) {
            AsnEntity asn = optional.get();
            String beforeStatus = asn.getStatus();
            
            String warehouseCode = (asn.getWarehouseCode() == null || asn.getWarehouseCode().isBlank()) ? "WH001" : asn.getWarehouseCode();
            String locationCode = "LOC001";

            if (asn.getItems() != null) {
                for (AsnItemEntity item : asn.getItems()) {
                    item.setReceivedQuantity(item.getExpectedQuantity());
                    
                    String batchNo = item.getBatchNo();
                    if (batchNo == null || batchNo.isEmpty()) {
                        batchNo = "DEFAULT_BATCH"; // 确保批次号不为空
                    }
                    
                    // 检查是否存在现有库存
                    Optional<InventoryEntity> existingInventory = inventoryRepository.findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo(
                        item.getMaterialCode(), warehouseCode, locationCode, batchNo
                    );
                    
                    if (existingInventory.isPresent()) {
                        // 更新现有库存
                        InventoryEntity inventory = existingInventory.get();
                        inventory.setQuantity(inventory.getQuantity().add(item.getReceivedQuantity()));
                        inventoryRepository.save(inventory);
                    } else {
                        // 创建新库存
                        InventoryEntity inventory = new InventoryEntity();
                        inventory.setWarehouseId(asn.getWarehouseId());
                        inventory.setMaterialCode(item.getMaterialCode());
                        inventory.setMaterialName(item.getMaterialName());
                        inventory.setQuantity(item.getReceivedQuantity());
                        inventory.setUnit(item.getUnit());
                        inventory.setBatchNo(batchNo);
                        inventory.setWarehouseCode(warehouseCode);
                        inventory.setLocationCode(locationCode);
                        inventoryRepository.save(inventory);
                    }

                    InventoryTransactionEntity tx = new InventoryTransactionEntity();
                    tx.setType("IN");
                    tx.setWarehouseCode(warehouseCode);
                    tx.setLocationCode(locationCode);
                    tx.setMaterialCode(item.getMaterialCode());
                    tx.setMaterialName(item.getMaterialName());
                    tx.setQuantity(item.getReceivedQuantity());
                    tx.setUnit(item.getUnit());
                    tx.setBatchNo(batchNo);
                    tx.setSourceNo(asn.getAsnNo());
                    tx.setOperator("system");
                    inventoryTransactionRepository.save(tx);
                }
            }
            
            asn.setStatus("RECEIVED");
            asn.setActualArrivalDate(LocalDateTime.now());
            AsnEntity saved = asnRepository.save(asn);
            if (!"RECEIVED".equalsIgnoreCase(beforeStatus)) {
                scmReceiptSyncService.enqueueAsnReceivedIfAbsent(saved);
                // P2-B: 收货完成同步触发 QMS 来料检验（IQC），补齐质量闭环入口
                qmsReceiptTriggerSyncService.enqueueQmsTriggerIfAbsent(saved);
                eventPublisher.publishEvent(new AsnReceivedEvent(saved.getId()));
            }
            try {
                if (erpClient != null) {
                    IntegrationVoucherDTO voucher = new IntegrationVoucherDTO();
                    voucher.setVoucherCode("WMS-IN-" + saved.getAsnNo());
                    voucher.setVoucherType("INBOUND");
                    voucher.setVoucherDate(LocalDateTime.now());
                    voucher.setAmount(BigDecimal.ZERO);
                    voucher.setCurrency("CNY");
                    voucher.setDescription("WMS入库收货：" + saved.getAsnNo());
                    voucher.setSourceSystem("WMS");
                    voucher.setSourceId(String.valueOf(saved.getId()));
                    erpClient.syncVoucher(voucher);
                }
            } catch (Exception ignored) {
            }
            return saved;
        }
        return null;
    }

    @Transactional
    @Override
    public AsnEntity startAsn(Long id) {
        Optional<AsnEntity> optional = asnRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        AsnEntity asn = optional.get();
        if ("CREATED".equalsIgnoreCase(asn.getStatus())) {
            asn.setStatus("RECEIVING");
        }
        if (asn.getActualArrivalDate() == null) {
            asn.setActualArrivalDate(LocalDateTime.now());
        }
        return asnRepository.save(asn);
    }

    @Transactional
    @Override
    public AsnEntity scanAsn(Long id, String barcode, BigDecimal quantity) {
        Optional<AsnEntity> optional = asnRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        AsnEntity asn = optional.get();
        String beforeStatus = asn.getStatus();
        BigDecimal qty = (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) ? BigDecimal.ONE : quantity;
        if (asn.getItems() != null) {
            for (AsnItemEntity item : asn.getItems()) {
                if (item.getMaterialCode() != null && item.getMaterialCode().equalsIgnoreCase(barcode)) {
                    BigDecimal current = item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity();
                    item.setReceivedQuantity(current.add(qty));
                    break;
                }
            }
        }
        boolean allReceived = true;
        boolean anyReceived = false;
        if (asn.getItems() != null) {
            for (AsnItemEntity item : asn.getItems()) {
                BigDecimal expected = item.getExpectedQuantity() == null ? BigDecimal.ZERO : item.getExpectedQuantity();
                BigDecimal received = item.getReceivedQuantity() == null ? BigDecimal.ZERO : item.getReceivedQuantity();
                if (received.compareTo(BigDecimal.ZERO) > 0) {
                    anyReceived = true;
                }
                if (received.compareTo(expected) < 0) {
                    allReceived = false;
                }
            }
        }
        if (allReceived && anyReceived) {
            asn.setStatus("RECEIVED");
        } else if (anyReceived) {
            asn.setStatus("PARTIAL_RECEIVED");
        } else {
            asn.setStatus("RECEIVING");
        }
        AsnEntity saved = asnRepository.save(asn);
        if ("RECEIVED".equalsIgnoreCase(saved.getStatus()) && !"RECEIVED".equalsIgnoreCase(beforeStatus)) {
            scmReceiptSyncService.enqueueAsnReceivedIfAbsent(saved);
            // P2-B: 扫码收货完成同步触发 QMS 来料检验（IQC）
            qmsReceiptTriggerSyncService.enqueueQmsTriggerIfAbsent(saved);
            eventPublisher.publishEvent(new AsnReceivedEvent(saved.getId()));
        }
        return saved;
    }

    @Transactional
    @Override
    public AsnEntity cancelAsn(Long id) {
        Optional<AsnEntity> optional = asnRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        AsnEntity asn = optional.get();
        asn.setStatus("CANCELLED");
        return asnRepository.save(asn);
    }
}
