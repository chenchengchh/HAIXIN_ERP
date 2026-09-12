package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.model.OrderRelationModel;
import com.hxcoe.erp.service.OrderRelationService;
import com.hxcoe.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单关联服务实现类
 * 用于管理销售订单、采购订单、生产订单之间的关联关系
 */
@Service
public class OrderRelationServiceImpl implements OrderRelationService {
    
    // 模拟数据库存储
    private static final Map<String, OrderRelationModel> ORDER_RELATION_MAP = new HashMap<>();
    
    @Override
    public OrderRelationModel createOrderRelation(OrderRelationModel orderRelationModel) {
        // 生成唯一ID
        String id = UUID.randomUUID().toString();
        orderRelationModel.setStatus("ACTIVE");
        ORDER_RELATION_MAP.put(id, orderRelationModel);
        return orderRelationModel;
    }
    
    @Override
    public OrderRelationModel getOrderRelationById(String id) {
        return ORDER_RELATION_MAP.get(id);
    }
    
    @Override
    public List<OrderRelationModel> getOrderRelationsBySalesOrderId(String salesOrderId) {
        List<OrderRelationModel> result = new ArrayList<>();
        for (OrderRelationModel model : ORDER_RELATION_MAP.values()) {
            if (salesOrderId.equals(model.getSalesOrderId())) {
                result.add(model);
            }
        }
        return result;
    }
    
    @Override
    public List<OrderRelationModel> getOrderRelationsByPurchaseOrderId(String purchaseOrderId) {
        List<OrderRelationModel> result = new ArrayList<>();
        for (OrderRelationModel model : ORDER_RELATION_MAP.values()) {
            if (purchaseOrderId.equals(model.getPurchaseOrderId())) {
                result.add(model);
            }
        }
        return result;
    }
    
    @Override
    public List<OrderRelationModel> getOrderRelationsByProductionOrderId(String productionOrderId) {
        List<OrderRelationModel> result = new ArrayList<>();
        for (OrderRelationModel model : ORDER_RELATION_MAP.values()) {
            if (productionOrderId.equals(model.getProductionOrderId())) {
                result.add(model);
            }
        }
        return result;
    }
    
    @Override
    public List<OrderRelationModel> getOrderRelationsByMaterialId(String materialId) {
        List<OrderRelationModel> result = new ArrayList<>();
        for (OrderRelationModel model : ORDER_RELATION_MAP.values()) {
            if (materialId.equals(model.getMaterialId())) {
                result.add(model);
            }
        }
        return result;
    }
    
    @Override
    public OrderRelationModel updateOrderRelation(OrderRelationModel orderRelationModel) {
        // 这里应该根据ID更新订单关联关系
        // 目前简化处理，直接覆盖
        ORDER_RELATION_MAP.put(UUID.randomUUID().toString(), orderRelationModel);
        return orderRelationModel;
    }
    
    @Override
    public boolean deleteOrderRelation(String id) {
        return ORDER_RELATION_MAP.remove(id) != null;
    }
    
    @Override
    public PageResult<OrderRelationModel> queryOrderRelations(Integer page, Integer size, String salesOrderId, String purchaseOrderId, String productionOrderId, String materialId, String status) {
        // 模拟查询结果
        List<OrderRelationModel> allList = new ArrayList<>(ORDER_RELATION_MAP.values());
        List<OrderRelationModel> filteredList = new ArrayList<>();
        
        // 过滤逻辑
        for (OrderRelationModel model : allList) {
            boolean match = true;
            if (salesOrderId != null && !salesOrderId.isEmpty() && !salesOrderId.equals(model.getSalesOrderId())) {
                match = false;
            }
            if (purchaseOrderId != null && !purchaseOrderId.isEmpty() && !purchaseOrderId.equals(model.getPurchaseOrderId())) {
                match = false;
            }
            if (productionOrderId != null && !productionOrderId.isEmpty() && !productionOrderId.equals(model.getProductionOrderId())) {
                match = false;
            }
            if (materialId != null && !materialId.isEmpty() && !materialId.equals(model.getMaterialId())) {
                match = false;
            }
            if (status != null && !status.isEmpty() && !status.equals(model.getStatus())) {
                match = false;
            }
            if (match) {
                filteredList.add(model);
            }
        }
        
        // 分页处理
        long total = filteredList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, (int) total);
        List<OrderRelationModel> pageList = filteredList.subList(start, end);
        
        return PageResult.build(total, size, page, pageList);
    }
}
