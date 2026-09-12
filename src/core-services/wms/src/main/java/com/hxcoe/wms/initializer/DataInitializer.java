package com.hxcoe.wms.initializer;

import com.hxcoe.wms.entity.*;
import com.hxcoe.wms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final BarcodeRuleRepository barcodeRuleRepository;

    public DataInitializer(
            WarehouseRepository warehouseRepository,
            ZoneRepository zoneRepository,
            LocationTypeRepository locationTypeRepository,
            LocationRepository locationRepository,
            MaterialTypeRepository materialTypeRepository,
            BarcodeRuleRepository barcodeRuleRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
        this.locationTypeRepository = locationTypeRepository;
        this.locationRepository = locationRepository;
        this.materialTypeRepository = materialTypeRepository;
        this.barcodeRuleRepository = barcodeRuleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("开始加载WMS基础配置数据...");
        ensureWarehouseAndZone();
        ensureBaseTypes();
        ensureLocations();
        logger.info("WMS基础配置数据加载完成");
    }

    private void ensureWarehouseAndZone() {
        WarehouseEntity wh = warehouseRepository.findByWarehouseCode("WH001");
        if (wh == null) {
            wh = new WarehouseEntity();
            wh.setWarehouseCode("WH001");
            wh.setWarehouseName("一号仓");
            wh.setAddress("默认地址");
            wh.setManager("admin");
            wh.setContact("00000000000");
            wh.setStatus(1);
            warehouseRepository.save(wh);
        }

        ZoneEntity z = zoneRepository.findByZoneCode("Z001");
        if (z == null) {
            z = new ZoneEntity();
            z.setWarehouseCode("WH001");
            z.setZoneCode("Z001");
            z.setZoneName("默认库区");
            z.setZoneType("1");
            z.setDescription("默认库区");
            z.setStatus("1");
            zoneRepository.save(z);
        }
    }

    private void ensureBaseTypes() {
        LocationTypeEntity lt = locationTypeRepository.findByTypeCode("LT01");
        if (lt == null) {
            lt = new LocationTypeEntity();
            lt.setTypeCode("LT01");
            lt.setTypeName("标准库位");
            lt.setTypeDesc("默认库位类型");
            lt.setMaxWeight(new BigDecimal("1000"));
            lt.setMixFlag(Boolean.TRUE);
            lt.setStatus("1");
            locationTypeRepository.save(lt);
        }

        MaterialTypeEntity mt = materialTypeRepository.findByTypeCode("MT01");
        if (mt == null) {
            mt = new MaterialTypeEntity();
            mt.setTypeCode("MT01");
            mt.setTypeName("默认物料类型");
            mt.setDescription("默认物料类型");
            mt.setStatus("1");
            materialTypeRepository.save(mt);
        }

        BarcodeRuleEntity br = barcodeRuleRepository.findByRuleCode("BR01");
        if (br == null) {
            br = new BarcodeRuleEntity();
            br.setRuleCode("BR01");
            br.setRuleName("默认条码规则");
            br.setRuleType("material");
            br.setRuleFormat("MAT{yyyy}{MM}{dd}{####}");
            br.setDescription("默认条码规则");
            br.setStatus("1");
            barcodeRuleRepository.save(br);
        }
    }

    private void ensureLocations() {
        ensureLocation("Z001-0001", "库位0001");
        ensureLocation("Z001-0002", "库位0002");
        ensureLocation("Z001-0003", "库位0003");
    }

    private void ensureLocation(String code, String name) {
        LocationEntity loc = locationRepository.findByLocationCode(code);
        if (loc != null) {
            return;
        }
        loc = new LocationEntity();
        loc.setWarehouseCode("WH001");
        loc.setZoneCode("Z001");
        loc.setLocationCode(code);
        loc.setLocationName(name);
        loc.setLocationTypeCode("LT01");
        loc.setStatus("1");
        loc.setRemark("");
        locationRepository.save(loc);
    }
}
