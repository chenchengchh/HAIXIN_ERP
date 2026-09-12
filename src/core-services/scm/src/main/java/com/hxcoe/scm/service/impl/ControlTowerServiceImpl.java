package com.hxcoe.scm.service.impl;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.client.WmsClient;
import com.hxcoe.scm.service.ControlTowerService;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.entity.MrpResultEntity;
import com.hxcoe.scm.repository.InventoryStrategyRepository;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import com.hxcoe.scm.repository.MrpResultRepository;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.math.BigDecimal;

@Service
public class ControlTowerServiceImpl implements ControlTowerService {

    private static final Logger logger = LoggerFactory.getLogger(ControlTowerServiceImpl.class);

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private SrmClient srmClient;

    @Autowired
    private InventoryStrategyRepository inventoryStrategyRepository;

    @Autowired
    private MrpResultRepository mrpResultRepository;

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getNetworkData() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        
        // 1. 获取真实仓库节点 (从WMS)
        int warehouseStart = nodes.size();
        try {
            Result<List<Map<String, Object>>> wmsResult = wmsClient.getWarehouses();
            if (isRemoteSuccess(wmsResult) && wmsResult.getData() != null) {
                for (Map<String, Object> wh : wmsResult.getData()) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", wh.get("warehouseCode"));
                    node.put("name", wh.get("warehouseName"));
                    node.put("type", "仓库");
                    // 如果WMS没有经纬度，这里给个随机坐标用于演示
                    Object lat = wh.get("latitude");
                    Object lon = wh.get("longitude");
                    node.put("x", lon != null ? lon : 300 + Math.random() * 200);
                    node.put("y", lat != null ? lat : 300 + Math.random() * 200);
                    node.put("location", wh.get("location"));
                    node.put("manager", wh.get("manager"));
                    
                    // 库存概要（如WMS缺少字段则兜底）
                    Map<String, Object> inv = new HashMap<>();
                    inv.put("total", wh.getOrDefault("totalStock", 0));
                    inv.put("available", wh.getOrDefault("availableStock", 0));
                    inv.put("turnoverRate", wh.getOrDefault("turnoverRate", 0));
                    inv.put("unit", "pcs");
                    node.put("inventory", inv);
                    
                    nodes.add(node);
                }
            }
        } catch (Exception e) {
            logger.error("获取WMS仓库数据失败: {}", e.getMessage());
        }
        if (nodes.size() == warehouseStart) {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT warehouse_code AS warehouseCode, warehouse_name AS warehouseName, address, manager, contact, status FROM scm_warehouse ORDER BY updated_time DESC",
                        Map.of()
                );
                for (Map<String, Object> wh : rows) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", wh.get("warehouseCode"));
                    node.put("name", wh.get("warehouseName"));
                    node.put("type", "仓库");
                    node.put("x", 300 + Math.random() * 200);
                    node.put("y", 300 + Math.random() * 200);
                    node.put("location", wh.get("address"));
                    node.put("manager", wh.get("manager"));

                    Map<String, Object> inv = new HashMap<>();
                    inv.put("total", 0);
                    inv.put("available", 0);
                    inv.put("turnoverRate", 0);
                    inv.put("unit", "pcs");
                    node.put("inventory", inv);

                    nodes.add(node);
                }
            } catch (Exception ignored) {
            }
        }

        // 2. 获取真实供应商节点 (从SRM)
        try {
            // 注意：这里需要SRM提供列表接口，假设返回PageResult的Map结构
            Result<Map<String, Object>> srmResult = srmClient.getSuppliers(null, null);
            if (isRemoteSuccess(srmResult) && srmResult.getData() != null) {
                Object listObj = srmResult.getData().get("list");
                if (listObj instanceof List) {
                    List<?> supplierList = (List<?>) listObj;
                    for (Object item : supplierList) {
                        if (item instanceof Map) {
                            Map<?, ?> sup = (Map<?, ?>) item;
                            Map<String, Object> node = new HashMap<>();
                            node.put("id", sup.get("supplierCode"));
                            node.put("name", sup.get("supplierName"));
                            node.put("type", "供应商");
                            
                            Object lat = sup.get("latitude");
                            Object lon = sup.get("longitude");
                            // 随机坐标兜底
                            node.put("x", lon != null ? lon : 100 + Math.random() * 200);
                            node.put("y", lat != null ? lat : 100 + Math.random() * 200);
                            
                            node.put("location", sup.get("address"));
                            node.put("manager", sup.get("contactPerson"));
                            nodes.add(node);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取SRM供应商数据失败: {}", e.getMessage());
        }
        
        // 3. 模拟工厂节点 (实际应从主数据获取)
        Map<String, Object> factory = new HashMap<>();
        factory.put("id", "FAC-001");
        factory.put("name", "上海制造中心");
        factory.put("type", "工厂");
        factory.put("x", 100);
        factory.put("y", 100);
        factory.put("location", "上海");
        Map<String, Object> prod = new HashMap<>();
        prod.put("utilization", 88);
        prod.put("dailyOutput", 1000);
        prod.put("onTimeRate", 98);
        prod.put("unit", "pcs");
        factory.put("production", prod);
        nodes.add(factory);
        
        data.put("nodes", nodes);
        
        // 4. 生成物流路径 (基于工厂到仓库，供应商到工厂)
        List<Map<String, Object>> edges = new ArrayList<>();
        int edgeCount = 1;
        
        for (Map<String, Object> node : nodes) {
            if ("仓库".equals(node.get("type"))) {
                // 工厂 -> 仓库
                Map<String, Object> edge = new HashMap<>();
                edge.put("id", "LOG-" + String.format("%03d", edgeCount++));
                edge.put("source", "FAC-001");
                edge.put("target", node.get("id"));
                edge.put("sourceName", "上海制造中心");
                edge.put("targetName", node.get("name"));
                edge.put("status", getRandomStatus());
                edge.put("transportMode", "公路");
                edges.add(edge);
            } else if ("供应商".equals(node.get("type"))) {
                // 供应商 -> 工厂
                Map<String, Object> edge = new HashMap<>();
                edge.put("id", "LOG-" + String.format("%03d", edgeCount++));
                edge.put("source", node.get("id"));
                edge.put("target", "FAC-001");
                edge.put("sourceName", node.get("name"));
                edge.put("targetName", "上海制造中心");
                edge.put("status", getRandomStatus());
                edge.put("transportMode", "公路");
                edges.add(edge);
            }
        }
        
        data.put("edges", edges);
        
        return data;
    }

    @Override
    public Map<String, Object> getKpis() {
        Map<String, Object> kpis = new HashMap<>();

        long integrationFailed = integrationTaskRepository.findAll().stream()
                .filter(t -> "FAILED".equals(t.getStatus()))
                .count();
        long mrpFailed = mrpResultRepository.findAll().stream()
                .filter(r -> "FAILED".equals(r.getStatus()))
                .count();
        long mrpConfirmed = mrpResultRepository.findAll().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .count();

        List<InventoryStrategyEntity> published = inventoryStrategyRepository.findByStatus("PUBLISHED");
        long lowStockCount = 0;
        for (InventoryStrategyEntity s : published) {
            BigDecimal reorderPoint = s.getReorderPoint() == null ? BigDecimal.ZERO : s.getReorderPoint();
            if (reorderPoint.compareTo(BigDecimal.ZERO) <= 0) continue;
            try {
                Result<List<WmsInventoryDTO>> invRes = wmsClient.getInventoryByMaterialCode(s.getMaterialCode());
                if (isRemoteSuccess(invRes) && invRes.getData() != null) {
                    BigDecimal stock = invRes.getData().stream().map(WmsInventoryDTO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (stock.compareTo(reorderPoint) < 0) {
                        lowStockCount++;
                    }
                } else {
                    lowStockCount++;
                }
            } catch (Exception e) {
                lowStockCount++;
            }
        }

        kpis.put("integrationFailedCount", integrationFailed);
        kpis.put("mrpReleaseFailedCount", mrpFailed);
        kpis.put("mrpPendingReleaseCount", mrpConfirmed);
        kpis.put("lowStockRiskCount", lowStockCount);
        return kpis;
    }

    @Override
    public List<Map<String, Object>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        for (IntegrationTaskEntity t : integrationTaskRepository.findAll()) {
            if (!"FAILED".equals(t.getStatus())) continue;
            Map<String, Object> a = new HashMap<>();
            a.put("type", "INTEGRATION_FAILED");
            a.put("severity", "HIGH");
            a.put("source", t.getActionType());
            a.put("refId", t.getId());
            a.put("planId", t.getPlanId());
            a.put("resultId", t.getResultId());
            a.put("externalRefNo", t.getExternalRefNo());
            a.put("message", t.getLastError());
            a.put("time", t.getUpdatedTime());
            alerts.add(a);
        }

        for (MrpResultEntity r : mrpResultRepository.findAll()) {
            if (!"FAILED".equals(r.getStatus())) continue;
            Map<String, Object> a = new HashMap<>();
            a.put("type", "MRP_RELEASE_FAILED");
            a.put("severity", "HIGH");
            a.put("source", "MRP");
            a.put("refId", r.getId());
            a.put("planId", r.getPlanId());
            a.put("materialCode", r.getMaterialCode());
            a.put("materialName", r.getMaterialName());
            a.put("message", r.getRejectedReason());
            a.put("time", r.getReleasedTime());
            alerts.add(a);
        }

        List<InventoryStrategyEntity> published = inventoryStrategyRepository.findByStatus("PUBLISHED");
        for (InventoryStrategyEntity s : published) {
            BigDecimal reorderPoint = s.getReorderPoint() == null ? BigDecimal.ZERO : s.getReorderPoint();
            if (reorderPoint.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal stock = BigDecimal.ZERO;
            boolean ok = false;
            try {
                Result<List<WmsInventoryDTO>> invRes = wmsClient.getInventoryByMaterialCode(s.getMaterialCode());
                if (isRemoteSuccess(invRes) && invRes.getData() != null) {
                    stock = invRes.getData().stream().map(WmsInventoryDTO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    ok = stock.compareTo(reorderPoint) >= 0;
                }
            } catch (Exception e) {
                ok = false;
            }
            if (ok) continue;
            Map<String, Object> a = new HashMap<>();
            a.put("type", "LOW_STOCK");
            a.put("severity", "MEDIUM");
            a.put("source", "INVENTORY");
            a.put("refId", s.getId());
            a.put("materialCode", s.getMaterialCode());
            a.put("materialName", s.getMaterialName());
            a.put("currentStock", stock);
            a.put("reorderPoint", reorderPoint);
            a.put("time", s.getUpdateTime());
            alerts.add(a);
        }

        alerts.sort((m1, m2) -> {
            String s1 = String.valueOf(m1.getOrDefault("severity", "MEDIUM"));
            String s2 = String.valueOf(m2.getOrDefault("severity", "MEDIUM"));
            return s2.compareTo(s1);
        });
        return alerts;
    }

    private String getRandomStatus() {
        return "正常";
    }

    private boolean isRemoteSuccess(Result<?> result) {
        return result != null && ResponseStatusAdapter.isSuccess(result.getCode());
    }
}
