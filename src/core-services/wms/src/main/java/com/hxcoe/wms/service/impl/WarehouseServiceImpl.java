package com.hxcoe.wms.service.impl;

import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.service.MasterDataEventOutboxService;
import com.hxcoe.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private MasterDataEventOutboxService masterDataEventOutboxService;

    @Override
    @Transactional
    public WarehouseEntity createWarehouse(WarehouseEntity warehouse) {
        WarehouseEntity saved = warehouseRepository.save(warehouse);
        masterDataEventOutboxService.enqueueWarehouseCreatedOrUpdated(saved, "WAREHOUSE_CREATED");
        return saved;
    }

    @Override
    @Transactional
    public WarehouseEntity updateWarehouse(Long id, WarehouseEntity warehouse) {
        Optional<WarehouseEntity> existing = warehouseRepository.findById(id);
        if (existing.isPresent()) {
            WarehouseEntity entity = existing.get();
            entity.setWarehouseName(warehouse.getWarehouseName());
            entity.setAddress(warehouse.getAddress());
            entity.setManager(warehouse.getManager());
            entity.setContact(warehouse.getContact());
            if (warehouse.getStatus() != null) {
                entity.setStatus(warehouse.getStatus());
            }
            WarehouseEntity saved = warehouseRepository.save(entity);
            masterDataEventOutboxService.enqueueWarehouseCreatedOrUpdated(saved, "WAREHOUSE_UPDATED");
            return saved;
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        Optional<WarehouseEntity> existing = warehouseRepository.findById(id);
        warehouseRepository.deleteById(id);
        existing.ifPresent(e -> masterDataEventOutboxService.enqueueWarehouseDeleted(e.getWarehouseCode()));
    }

    @Override
    public Page<WarehouseEntity> getWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable);
    }

    @Override
    public Optional<WarehouseEntity> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }
}
