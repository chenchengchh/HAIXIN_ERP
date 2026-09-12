package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.CrmOrderClient;
import com.hxcoe.erp.client.LesOrderClient;
import com.hxcoe.erp.client.WmsInboundAsnClient;
import com.hxcoe.erp.client.WmsInventoryClient;
import com.hxcoe.erp.client.WmsOutboundOrderClient;
import com.hxcoe.erp.client.WmsStockCountClient;
import com.hxcoe.erp.dto.supplychain.InventoryDto;
import com.hxcoe.erp.dto.supplychain.MrpResultDto;
import com.hxcoe.erp.dto.supplychain.PurchaseOrderDto;
import com.hxcoe.erp.dto.supplychain.PurchaseOrderItemDto;
import com.hxcoe.erp.dto.supplychain.PurchaseReturnDto;
import com.hxcoe.erp.dto.supplychain.SalesOrderDto;
import com.hxcoe.erp.dto.supplychain.SalesOrderItemDto;
import com.hxcoe.erp.dto.supplychain.TransferOrderDto;
import com.hxcoe.erp.entity.MaterialEntity;
import com.hxcoe.erp.entity.SupplyChainEntity;
import com.hxcoe.erp.repository.MaterialRepository;
import com.hxcoe.erp.service.ErpPurchaseReturnService;
import com.hxcoe.erp.service.ErpTransferOrderService;
import com.hxcoe.erp.service.SupplyChainService;
import com.hxcoe.erp.support.ResultDataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 供应链模块控制器
 */
@RestController
@RequestMapping("/api/v1/erp/supply-chain")
public class SupplyChainController {

    @Autowired
    private SupplyChainService supplyChainService;

    @Autowired
    private WmsInboundAsnClient wmsInboundAsnClient;

    @Autowired
    private WmsOutboundOrderClient wmsOutboundOrderClient;

    @Autowired
    private WmsInventoryClient wmsInventoryClient;

    @Autowired
    private WmsStockCountClient wmsStockCountClient;

    @Autowired
    private CrmOrderClient crmOrderClient;

    @Autowired
    private LesOrderClient lesOrderClient;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ErpTransferOrderService erpTransferOrderService;

    @Autowired
    private ErpPurchaseReturnService erpPurchaseReturnService;

    private static volatile String lastMrpRunDate;

    /**
     * 创建供应链记录
     *
     * @param supplyChainEntity 供应链实体
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<SupplyChainEntity> createSupplyChain(@RequestBody SupplyChainEntity supplyChainEntity) {
        SupplyChainEntity result = supplyChainService.createSupplyChain(supplyChainEntity);
        return success("记录查询成功", result);
    }

    /**
     * 根据ID查询供应链记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplyChainEntity> getSupplyChainById(@PathVariable Long id) {
        SupplyChainEntity result = supplyChainService.getSupplyChainById(id);
        if (result != null) {
            return success("记录查询成功", result);
        }
        return notFound("供应链记录不存在");
    }

    /**
     * 根据供应链单号查询供应链记录
     *
     * @param scNo 供应链单号
     * @return 查询结果
     */
    @GetMapping("/no/{scNo}")
    public ApiResponse<SupplyChainEntity> getSupplyChainByNo(@PathVariable String scNo) {
        SupplyChainEntity result = supplyChainService.getSupplyChainByNo(scNo);
        if (result != null) {
            return success("记录查询成功", result);
        }
        return notFound("供应链记录不存在");
    }

    /**
     * 更新供应链记录
     *
     * @param supplyChainEntity 供应链实体
     * @return 更新结果
     */
    @PutMapping
    public ApiResponse<SupplyChainEntity> updateSupplyChain(@RequestBody SupplyChainEntity supplyChainEntity) {
        SupplyChainEntity result = supplyChainService.updateSupplyChain(supplyChainEntity);
        return success("记录查询成功", result);
    }

    /**
     * 删除供应链记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteSupplyChain(@PathVariable Long id) {
        boolean result = supplyChainService.deleteSupplyChain(id);
        if (result) {
            return success("记录查询成功", result);
        }
        return notFound("供应链记录不存在");
    }

    /**
     * 分页查询供应链记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param businessType 业务类型
     * @param materialCode 物料编码
     * @param warehouseCode 仓库编码
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<SupplyChainEntity>> getSupplyChainList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<SupplyChainEntity> result = supplyChainService.getSupplyChainList(page, size, businessType, materialCode, warehouseCode, status, startDate, endDate);
        return success("记录查询成功", result);
    }

    /**
     * 根据物料编码查询供应链记录
     *
     * @param materialCode 物料编码
     * @return 供应链实体列表
     */
    @GetMapping("/material/{materialCode}")
    public ApiResponse<List<SupplyChainEntity>> getSupplyChainByMaterialCode(@PathVariable String materialCode) {
        List<SupplyChainEntity> result = supplyChainService.getSupplyChainByMaterialCode(materialCode);
        return success("记录查询成功", result);
    }

    /**
     * 根据仓库编码查询供应链记录
     *
     * @param warehouseCode 仓库编码
     * @return 供应链实体列表
     */
    @GetMapping("/warehouse/{warehouseCode}")
    public ApiResponse<List<SupplyChainEntity>> getSupplyChainByWarehouseCode(@PathVariable String warehouseCode) {
        List<SupplyChainEntity> result = supplyChainService.getSupplyChainByWarehouseCode(warehouseCode);
        return success("记录查询成功", result);
    }

    /**
     * 测试供应链模块接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    public ApiResponse<String> testSupplyChain() {
        return success("操作成功", "供应链模块测试通过");
    }

    @GetMapping("/purchase/orders")
    public ApiResponse<PageResult<PurchaseOrderDto>> listPurchaseOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Result<Object> res = wmsInboundAsnClient.list(Math.max(page, 1), Math.max(size, 1));
        ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
        List<PurchaseOrderDto> mapped = new ArrayList<>();
        for (Map<String, Object> row : parts.rows()) {
            mapped.add(mapAsnToPurchaseOrder(row));
        }
        return success("采购订单查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
    }

    @GetMapping("/purchase/orders/{id}")
    public ApiResponse<PurchaseOrderDto> getPurchaseOrder(@PathVariable Long id) {
        Result<Object> res = wmsInboundAsnClient.detail(id);
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        if (isEmptyRow(row)) {
            return notFound("采购订单不存在");
        }
        return success("采购订单查询成功", mapAsnToPurchaseOrderWithItems(row));
    }

    @PostMapping("/purchase/orders")
    public ApiResponse<PurchaseOrderDto> createPurchaseOrder(@RequestBody Map<String, Object> body) {
        Map<String, Object> asn = mapPurchaseBodyToAsn(body);
        Result<Object> res = wmsInboundAsnClient.create(asn);
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("采购订单创建成功", mapAsnToPurchaseOrderWithItems(row));
    }

    @PutMapping("/purchase/orders/{id}")
    public ApiResponse<PurchaseOrderDto> updatePurchaseOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> asn = mapPurchaseBodyToAsn(body);
        Result<Object> res = wmsInboundAsnClient.update(id, asn);
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("采购订单更新成功", mapAsnToPurchaseOrderWithItems(row));
    }

    @PostMapping("/purchase/orders/{id}/approve")
    public ApiResponse<PurchaseOrderDto> approvePurchaseOrder(@PathVariable Long id) {
        Result<Object> res = wmsInboundAsnClient.confirm(id);
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("采购订单审批成功", mapAsnToPurchaseOrderWithItems(row));
    }

    @PostMapping("/purchase/receipt")
    public ApiResponse<Object> receipt(@RequestBody Map<String, Object> body) {
        Object poId = body != null ? body.get("purchase_order_id") : null;
        Long id = poId instanceof Number n ? n.longValue() : null;
        if (id == null && poId != null) {
            try { id = Long.parseLong(String.valueOf(poId)); } catch (Exception ignored) {}
        }
        if (id == null) {
            return badRequest("purchase_order_id 不能为空");
        }
        Result<Object> res = wmsInboundAsnClient.receive(id);
        return success("采购收货成功", res != null ? res.getData() : null);
    }

    @PostMapping("/purchase/return")
    public ApiResponse<Object> createPurchaseReturn(@RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String warehouseCode = body.get("warehouse_code") != null ? String.valueOf(body.get("warehouse_code")) : null;
        if (warehouseCode == null || warehouseCode.isBlank()) {
            warehouseCode = body.get("warehouseCode") != null ? String.valueOf(body.get("warehouseCode")) : null;
        }
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return badRequest("warehouse_code 不能为空");
        }
        Object items = mapOutboundItems(body.getOrDefault("return_items", body.getOrDefault("items", List.of())));

        Map<String, Object> outbound = new HashMap<>();
        outbound.put("warehouseCode", warehouseCode);
        outbound.put("customerCode", body.getOrDefault("supplier_code", body.get("supplierCode")));
        outbound.put("customerName", body.getOrDefault("supplier_name", body.get("supplierName")));
        outbound.put("orderType", "PURCHASE_RETURN");
        outbound.put("sourceNo", firstNonBlank(body, "sourceNo", "source_no", "returnNo", "return_no", "orderNo", "order_no"));
        outbound.put("remark", body.getOrDefault("remark", ""));
        outbound.put("items", items);
        Result<Object> res = wmsOutboundOrderClient.create(outbound);
        if (!isRemoteSuccess(res)) {
            return badRequest(res == null ? "WMS创建出库单失败" : res.getMessage());
        }
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        PurchaseReturnDto dto = erpPurchaseReturnService.createFromRequest(body, row);
        return success("采购退货创建成功", dto != null ? dto : (res != null ? res.getData() : null));
    }

    @GetMapping("/purchase/returns")
    public ApiResponse<PageResult<PurchaseReturnDto>> listPurchaseReturns(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return success("采购退货查询成功", erpPurchaseReturnService.list(page, size));
    }

    @GetMapping("/purchase/returns/{id}")
    public ApiResponse<PurchaseReturnDto> getPurchaseReturn(@PathVariable Long id) {
        PurchaseReturnDto dto = erpPurchaseReturnService.getById(id);
        if (dto == null) {
            return notFound("采购退货不存在");
        }
        return success("采购退货查询成功", dto);
    }

    @GetMapping("/sales/orders")
    public ApiResponse<PageResult<SalesOrderDto>> listSalesOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Result<Object> res = crmOrderClient.myOrders(page, size, keyword, status, startDate, endDate, customerName);
        ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
        List<SalesOrderDto> mapped = new ArrayList<>();
        for (Map<String, Object> row : parts.rows()) {
            mapped.add(mapCrmOrderToSalesOrder(row));
        }
        return success("销售订单查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
    }

    @GetMapping("/sales/orders/{id}")
    public ApiResponse<SalesOrderDto> getSalesOrder(@PathVariable Long id) {
        Result<Object> res = crmOrderClient.getById(id);
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        if (isEmptyRow(row)) {
            return notFound("销售订单不存在");
        }
        return success("销售订单详情查询成功", mapCrmOrderToSalesOrder(row));
    }

    @PostMapping("/sales/orders")
    public ApiResponse<SalesOrderDto> createSalesOrder(@RequestBody Map<String, Object> body) {
        Result<Object> res = crmOrderClient.create(mapSalesOrderToCrm(body));
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("销售订单创建成功", mapCrmOrderToSalesOrder(row));
    }

    @PutMapping("/sales/orders/{id}")
    public ApiResponse<SalesOrderDto> updateSalesOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Result<Object> res = crmOrderClient.update(id, mapSalesOrderToCrm(body));
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("销售订单更新成功", mapCrmOrderToSalesOrder(row));
    }

    @DeleteMapping("/sales/orders/{id}")
    public ApiResponse<Object> deleteSalesOrder(@PathVariable Long id) {
        Result<Object> res = crmOrderClient.delete(id);
        return success("销售订单删除成功", res != null ? res.getData() : null);
    }

    @PostMapping("/sales/orders/{id}/submit")
    public ApiResponse<Object> submitSalesOrder(@PathVariable Long id) {
        Result<Object> res = crmOrderClient.submit(id);
        return success("销售订单提交成功", res != null ? res.getData() : null);
    }

    @PostMapping("/sales/orders/{id}/approve")
    public ApiResponse<Object> approveSalesOrder(@PathVariable Long id) {
        Result<Object> res = crmOrderClient.approve(id);
        return success("销售订单审核成功", res != null ? res.getData() : null);
    }

    @PostMapping("/sales/delivery")
    public ApiResponse<Object> createSalesDelivery(@RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String warehouseCode = body.get("warehouse_code") != null ? String.valueOf(body.get("warehouse_code")) : null;
        if (warehouseCode == null || warehouseCode.isBlank()) {
            warehouseCode = body.get("warehouseCode") != null ? String.valueOf(body.get("warehouseCode")) : null;
        }
        if (warehouseCode == null || warehouseCode.isBlank()) {
            warehouseCode = body.get("warehouse_id") != null ? String.valueOf(body.get("warehouse_id")) : null;
        }
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return badRequest("warehouse_code 不能为空");
        }

        Map<String, Object> outbound = new HashMap<>();
        outbound.put("warehouseCode", warehouseCode);
        outbound.put("customerCode", body.getOrDefault("customer_code", body.get("customerCode")));
        outbound.put("customerName", body.getOrDefault("customer_name", body.get("customerName")));
        outbound.put("orderType", "SALES");
        outbound.put("sourceNo", firstNonBlank(body, "sourceNo", "source_no", "salesOrderNo", "sales_order_no", "orderNo", "order_no"));
        outbound.put("remark", body.getOrDefault("remark", ""));
        outbound.put("items", body.getOrDefault("items", List.of()));

        Result<Object> res = wmsOutboundOrderClient.create(outbound);
        if (!isRemoteSuccess(res)) {
            return badRequest(res == null ? "WMS创建出库单失败" : res.getMessage());
        }
        Object outData = res != null ? res.getData() : null;
        Map<String, Object> outRow = ResultDataExtractor.asMap(outData);
        String outboundNo = String.valueOf(outRow.getOrDefault("outboundNo", outRow.getOrDefault("outbound_no", "")));

        Map<String, Object> lesOrder = new HashMap<>();
        lesOrder.put("orderCode", outboundNo != null && !outboundNo.isBlank() ? "LES-" + outboundNo : "LES-" + System.currentTimeMillis());
        lesOrder.put("orderType", "SALES_DELIVERY");
        lesOrder.put("sourceLocation", warehouseCode);
        lesOrder.put("destinationLocation", body.getOrDefault("destination", body.getOrDefault("customer_name", "")));
        lesOrder.put("orderStatus", "created");
        lesOrder.put("remark", body.getOrDefault("remark", ""));
        lesOrder.put("createdBy", "erp");
        lesOrderClient.create(lesOrder);

        return success("销售发货创建成功", outData);
    }

    @GetMapping("/inventory")
    public ApiResponse<PageResult<InventoryDto>> inventory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String materialName
    ) {
        Result<Object> res = wmsInventoryClient.list(Math.max(page, 1), Math.max(size, 1), warehouseCode, null, materialCode, materialName, null);
        ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
        List<InventoryDto> mapped = new ArrayList<>();
        for (Map<String, Object> row : parts.rows()) {
            mapped.add(mapInventory(row));
        }
        return success("库存查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
    }

    @PostMapping("/inventory/transfer")
    public ApiResponse<Object> transfer(@RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String fromWarehouseCode = body.get("from_warehouse_id") != null ? String.valueOf(body.get("from_warehouse_id")) : null;
        String toWarehouseCode = body.get("to_warehouse_id") != null ? String.valueOf(body.get("to_warehouse_id")) : null;
        if (fromWarehouseCode == null || fromWarehouseCode.isBlank() || toWarehouseCode == null || toWarehouseCode.isBlank()) {
            return badRequest("from_warehouse_id/to_warehouse_id 不能为空");
        }
        Object items = mapOutboundItems(body.getOrDefault("transfer_items", List.of()));

        Map<String, Object> outbound = new HashMap<>();
        outbound.put("warehouseCode", fromWarehouseCode);
        outbound.put("customerCode", "INTERNAL");
        outbound.put("customerName", "内部调拨");
        outbound.put("orderType", "TRANSFER");
        outbound.put("sourceNo", firstNonBlank(body, "sourceNo", "source_no", "transferNo", "transfer_no", "orderNo", "order_no"));
        outbound.put("remark", body.getOrDefault("remark", ""));
        outbound.put("items", items);
        Result<Object> out = wmsOutboundOrderClient.create(outbound);
        if (!isRemoteSuccess(out)) {
            return badRequest(out == null ? "WMS创建出库单失败" : out.getMessage());
        }

        Map<String, Object> asn = new HashMap<>();
        asn.put("warehouseCode", toWarehouseCode);
        asn.put("supplierCode", "INTERNAL");
        asn.put("supplierName", "内部调拨");
        asn.put("asnType", "TRANSFER");
        asn.put("remark", body.getOrDefault("remark", ""));
        asn.put("items", items);
        Result<Object> in = wmsInboundAsnClient.create(asn);
        if (!isRemoteSuccess(in)) {
            return badRequest(in == null ? "WMS创建ASN失败" : in.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("outbound", out != null ? out.getData() : null);
        data.put("asn", in != null ? in.getData() : null);
        Map<String, Object> outRow = ResultDataExtractor.asMap(out != null ? out.getData() : null);
        Map<String, Object> inRow = ResultDataExtractor.asMap(in != null ? in.getData() : null);
        TransferOrderDto dto = erpTransferOrderService.createFromRequest(body, outRow, inRow);
        return success("库存调拨创建成功", dto != null ? dto : data);
    }

    @GetMapping("/inventory/transfers")
    public ApiResponse<PageResult<TransferOrderDto>> listTransferOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return success("库存调拨查询成功", erpTransferOrderService.list(page, size));
    }

    @GetMapping("/inventory/transfers/{id}")
    public ApiResponse<TransferOrderDto> getTransferOrder(@PathVariable Long id) {
        TransferOrderDto dto = erpTransferOrderService.getById(id);
        if (dto == null) {
            return notFound("库存调拨不存在");
        }
        return success("库存调拨查询成功", dto);
    }

    @PostMapping("/inventory/transfers/{id}/receive")
    public ApiResponse<TransferOrderDto> receiveTransferOrder(@PathVariable("id") Long id) {
        TransferOrderDto dto = erpTransferOrderService.getById(id);
        if (dto == null) {
            return notFound("库存调拨不存在");
        }
        if (dto.getAsnId() == null) {
            return badRequest("调拨ASN不存在");
        }
        Result<Object> res = wmsInboundAsnClient.confirm(dto.getAsnId());
        if (!isRemoteSuccess(res)) {
            return badRequest(res == null ? "WMS调拨收货失败" : res.getMessage());
        }
        Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        TransferOrderDto updated = erpTransferOrderService.applyInboundReceived(id, row);
        return success("库存调拨收货成功", updated != null ? updated : dto);
    }

    @PostMapping("/inventory/count")
    public ApiResponse<Object> count(@RequestBody Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        String warehouseCode = body.get("warehouse_id") != null ? String.valueOf(body.get("warehouse_id")) : null;
        if (warehouseCode == null || warehouseCode.isBlank()) {
            warehouseCode = body.get("warehouse_code") != null ? String.valueOf(body.get("warehouse_code")) : null;
        }
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return badRequest("warehouse_id 不能为空");
        }

        Map<String, Object> req = new HashMap<>();
        req.put("warehouseCode", warehouseCode);
        req.put("countType", "1");
        req.put("createUser", body.getOrDefault("count_person", "admin"));
        Result<Object> res = wmsStockCountClient.create(req);
        return success("库存盘点创建成功", res != null ? res.getData() : null);
    }

    @PostMapping("/mrp/run")
    public ApiResponse<Boolean> runMrp(@RequestBody Map<String, Object> body) {
        Object runDate = body != null ? body.get("run_date") : null;
        lastMrpRunDate = runDate != null ? String.valueOf(runDate) : String.valueOf(System.currentTimeMillis());
        return success("MRP运算已提交", true);
    }

    @GetMapping("/mrp/results")
    public ApiResponse<PageResult<MrpResultDto>> mrpResults(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Result<Object> res = wmsInventoryClient.list(Math.max(page, 1), Math.max(size, 1), null, null, null, null, null);
        ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
        List<MrpResultDto> mapped = new ArrayList<>();
        for (Map<String, Object> row : parts.rows()) {
            MrpResultDto dto = new MrpResultDto();
            String materialCode = str(row, "materialCode", "material_code");
            dto.setMaterialCode(materialCode);
            dto.setMaterialName(str(row, "materialName", "material_name"));
            dto.setSpecification(str(row, "specification", "specification"));
            dto.setMaterialType(str(row, "materialType", "material_type"));
            BigDecimal currentQty = toDecimal(row.get("quantity"));
            if (currentQty == null) currentQty = BigDecimal.ZERO;
            dto.setCurrentQty(currentQty);

            MaterialEntity material = materialRepository.findByMaterialCode(materialCode).orElse(null);
            BigDecimal safety = material != null && material.getSafetyStock() != null ? material.getSafetyStock() : BigDecimal.ZERO;
            dto.setSafetyStock(safety);
            dto.setDemandQty(BigDecimal.ZERO);
            dto.setSupplyQty(BigDecimal.ZERO);

            BigDecimal suggestionQty = safety.subtract(currentQty);
            if (suggestionQty.compareTo(BigDecimal.ZERO) < 0) suggestionQty = BigDecimal.ZERO;
            dto.setSuggestionQty(suggestionQty);
            dto.setSuggestionType(suggestionQty.compareTo(BigDecimal.ZERO) > 0 ? "PURCHASE" : "INVENTORY");
            dto.setSuggestionDate(lastMrpRunDate != null ? lastMrpRunDate : String.valueOf(System.currentTimeMillis()));
            dto.setRemark(suggestionQty.compareTo(BigDecimal.ZERO) > 0 ? "低于安全库存" : "");
            mapped.add(dto);
        }
        return success("MRP结果查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }

    private static boolean isEmptyRow(Map<String, Object> row) {
        return row == null || row.isEmpty();
    }

    private static boolean isRemoteSuccess(Result<?> result) {
        Integer code = result == null ? null : result.getCode();
        return code != null && (code == 0 || code == 200);
    }

    private PurchaseOrderDto mapAsnToPurchaseOrder(Map<String, Object> row) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setOrderNo(str(row, "asnNo", "asn_no"));
        dto.setSupplierCode(str(row, "supplierCode", "supplier_code"));
        dto.setSupplierName(str(row, "supplierName", "supplier_name"));
        dto.setPurchaseDate(str(row, "createTime", "create_time"));
        dto.setExpectedDeliveryDate(str(row, "expectedArrivalDate", "expected_arrival_date"));
        dto.setActualDeliveryDate(str(row, "actualArrivalDate", "actual_arrival_date"));
        dto.setBuyer(str(row, "createUser", "create_user"));
        dto.setCurrency(str(row, "currency", "currency"));
        dto.setTotalAmount(toDecimal(row.get("totalAmount")));
        dto.setRemark(str(row, "remark", "remark"));
        String status = str(row, "status", "status");
        dto.setStatus(mapPurchaseStatus(status));
        dto.setPaymentStatus("未付款");
        dto.setDeliveryStatus("未交货");
        dto.setCreateTime(str(row, "createTime", "create_time"));
        return dto;
    }

    private PurchaseOrderDto mapAsnToPurchaseOrderWithItems(Map<String, Object> row) {
        PurchaseOrderDto dto = mapAsnToPurchaseOrder(row);
        Object items = row.getOrDefault("items", row.get("asnItems"));
        List<Map<String, Object>> itemRows = ResultDataExtractor.asListOfMap(items);
        List<PurchaseOrderItemDto> mapped = new ArrayList<>();
        for (Map<String, Object> ir : itemRows) {
            PurchaseOrderItemDto it = new PurchaseOrderItemDto();
            it.setId(toLong(ir.get("id")));
            it.setMaterialCode(str(ir, "materialCode", "material_code"));
            it.setMaterialName(str(ir, "materialName", "material_name"));
            it.setSpecification(str(ir, "specification", "specification"));
            it.setUnit(str(ir, "unit", "unit"));
            it.setQuantity(toDecimal(ir.get("expectedQty")));
            if (it.getQuantity() == null) it.setQuantity(toDecimal(ir.get("quantity")));
            it.setUnitPrice(toDecimal(ir.get("unitPrice")));
            it.setAmount(toDecimal(ir.get("amount")));
            it.setRemark(str(ir, "remark", "remark"));
            mapped.add(it);
        }
        dto.setItems(mapped);
        return dto;
    }

    private Map<String, Object> mapPurchaseBodyToAsn(Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        if (body == null) return out;
        out.put("asnNo", body.get("order_no"));
        out.put("supplierCode", body.getOrDefault("supplier_code", body.get("supplier_id")));
        out.put("supplierName", body.get("supplier_name"));
        out.put("currency", body.getOrDefault("currency", "CNY"));
        out.put("remark", body.get("remark"));

        Object items = body.getOrDefault("items", List.of());
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(items);
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> ir : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("materialCode", ir.get("material_code"));
            it.put("materialName", ir.get("material_name"));
            it.put("specification", ir.get("specification"));
            it.put("unit", ir.get("unit"));
            it.put("expectedQty", ir.get("quantity"));
            it.put("receivedQty", 0);
            it.put("unitPrice", ir.get("unit_price"));
            it.put("amount", ir.get("amount"));
            it.put("remark", ir.get("remark"));
            mapped.add(it);
        }
        out.put("items", mapped);
        return out;
    }

    private SalesOrderDto mapCrmOrderToSalesOrder(Map<String, Object> row) {
        SalesOrderDto dto = new SalesOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setOrderNo(str(row, "orderNo", "order_no"));
        dto.setCustomerId(str(row, "customerId", "customer_id"));
        dto.setCustomerCode(str(row, "customerCode", "customer_code"));
        dto.setCustomerName(str(row, "customerName", "customer_name"));
        dto.setOrderDate(str(row, "orderDate", "order_date"));
        dto.setDeliveryDate(str(row, "deliveryDate", "delivery_date"));
        dto.setCurrency(str(row, "currency", "currency"));
        dto.setTotalAmount(toDecimal(row.get("totalAmount")));
        dto.setStatus(mapSalesStatus(str(row, "status", "status")));
        dto.setPaymentStatus(str(row, "paymentStatus", "payment_status"));
        dto.setRemark(str(row, "remark", "remark"));
        Object items = row.getOrDefault("items", List.of());
        List<Map<String, Object>> itemRows = ResultDataExtractor.asListOfMap(items);
        List<SalesOrderItemDto> mapped = new ArrayList<>();
        for (Map<String, Object> ir : itemRows) {
            SalesOrderItemDto it = new SalesOrderItemDto();
            it.setId(toLong(ir.get("id")));
            it.setMaterialCode(str(ir, "materialCode", "material_code"));
            it.setMaterialName(str(ir, "materialName", "material_name"));
            it.setSpecification(str(ir, "specification", "specification"));
            it.setUnit(str(ir, "unit", "unit"));
            it.setQuantity(toDecimal(ir.get("quantity")));
            it.setUnitPrice(toDecimal(ir.get("unitPrice")));
            it.setAmount(toDecimal(ir.get("amount")));
            it.setRemark(str(ir, "remark", "remark"));
            mapped.add(it);
        }
        dto.setItems(mapped);
        return dto;
    }

    private Map<String, Object> mapSalesOrderToCrm(Map<String, Object> body) {
        if (body == null) body = new HashMap<>();
        Map<String, Object> out = new HashMap<>();
        if (body.get("order_no") != null) out.put("orderNo", body.get("order_no"));
        if (body.get("orderNo") != null) out.put("orderNo", body.get("orderNo"));
        if (body.get("customer_id") != null) out.put("customerId", body.get("customer_id"));
        if (body.get("customerId") != null) out.put("customerId", body.get("customerId"));
        if (body.get("customer_code") != null) out.put("customerCode", body.get("customer_code"));
        if (body.get("customerCode") != null) out.put("customerCode", body.get("customerCode"));
        if (body.get("customer_name") != null) out.put("customerName", body.get("customer_name"));
        if (body.get("customerName") != null) out.put("customerName", body.get("customerName"));
        if (body.get("order_date") != null) out.put("orderDate", body.get("order_date"));
        if (body.get("orderDate") != null) out.put("orderDate", body.get("orderDate"));
        if (body.get("delivery_date") != null) out.put("deliveryDate", body.get("delivery_date"));
        if (body.get("deliveryDate") != null) out.put("deliveryDate", body.get("deliveryDate"));
        if (body.get("currency") != null) out.put("currency", body.get("currency"));
        if (body.get("total_amount") != null) out.put("totalAmount", body.get("total_amount"));
        if (body.get("totalAmount") != null) out.put("totalAmount", body.get("totalAmount"));
        if (body.get("status") != null) out.put("status", body.get("status"));
        if (body.get("payment_status") != null) out.put("paymentStatus", body.get("payment_status"));
        if (body.get("paymentStatus") != null) out.put("paymentStatus", body.get("paymentStatus"));
        if (body.get("remark") != null) out.put("remark", body.get("remark"));
        Object items = body.getOrDefault("items", body.getOrDefault("order_items", List.of()));
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(items);
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> ir : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("materialCode", ir.getOrDefault("material_code", ir.get("materialCode")));
            it.put("materialName", ir.getOrDefault("material_name", ir.get("materialName")));
            it.put("specification", ir.getOrDefault("specification", ir.get("specification")));
            it.put("unit", ir.getOrDefault("unit", ir.get("unit")));
            it.put("quantity", ir.getOrDefault("quantity", ir.get("quantity")));
            it.put("unitPrice", ir.getOrDefault("unit_price", ir.get("unitPrice")));
            it.put("amount", ir.getOrDefault("amount", ir.get("amount")));
            it.put("remark", ir.getOrDefault("remark", ir.get("remark")));
            mapped.add(it);
        }
        out.put("items", mapped);
        return out;
    }

    private List<Map<String, Object>> mapOutboundItems(Object items) {
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(items);
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> ir : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("materialCode", ir.getOrDefault("materialCode", ir.get("material_code")));
            it.put("materialName", ir.getOrDefault("materialName", ir.get("material_name")));
            it.put("specification", ir.getOrDefault("specification", ir.get("specification")));
            it.put("unit", ir.getOrDefault("unit", ir.get("unit")));
            Object qty = ir.getOrDefault("quantity", ir.getOrDefault("planQuantity", ir.get("plan_quantity")));
            it.put("quantity", qty);
            it.put("unitPrice", ir.getOrDefault("unitPrice", ir.get("unit_price")));
            it.put("amount", ir.getOrDefault("amount", ir.getOrDefault("totalPrice", ir.get("total_price"))));
            it.put("remark", ir.getOrDefault("remark", ir.get("remark")));
            mapped.add(it);
        }
        return mapped;
    }

    private String mapSalesStatus(String status) {
        if (status == null) return "draft";
        String s = status.trim().toLowerCase();
        return switch (s) {
            case "draft" -> "draft";
            case "submitted", "approved" -> "confirmed";
            case "in_progress", "processing" -> "processing";
            case "shipped" -> "shipped";
            case "completed" -> "completed";
            case "cancelled" -> "cancelled";
            default -> s;
        };
    }

    private InventoryDto mapInventory(Map<String, Object> row) {
        InventoryDto dto = new InventoryDto();
        dto.setId(toLong(row.get("id")));
        dto.setWarehouseCode(str(row, "warehouseCode", "warehouse_code"));
        dto.setWarehouseName(str(row, "warehouseName", "warehouse_name"));
        dto.setLocation(str(row, "locationCode", "location_code"));
        dto.setMaterialCode(str(row, "materialCode", "material_code"));
        dto.setMaterialName(str(row, "materialName", "material_name"));
        dto.setSpecification(str(row, "specification", "specification"));
        dto.setUnit(str(row, "unit", "unit"));
        dto.setBatchNo(str(row, "batchNo", "batch_no"));
        BigDecimal qty = toDecimal(row.get("quantity"));
        if (qty == null) qty = toDecimal(row.get("currentQty"));
        dto.setCurrentQty(qty);
        BigDecimal locked = toDecimal(row.get("lockedQty"));
        if (locked == null) locked = BigDecimal.ZERO;
        dto.setLockedQty(locked);
        dto.setAvailableQty(qty == null ? null : qty.subtract(locked));
        dto.setLastInTime(str(row, "lastInTime", "last_in_time"));
        dto.setLastOutTime(str(row, "lastOutTime", "last_out_time"));
        dto.setRemark(str(row, "remark", "remark"));

        MaterialEntity material = materialRepository.findByMaterialCode(dto.getMaterialCode()).orElse(null);
        if (material != null) {
            dto.setSafetyStock(material.getSafetyStock());
            dto.setMinStock(material.getMinStock());
            dto.setMaxStock(material.getMaxStock());
        }

        dto.setInventoryStatus(calcInventoryStatus(dto));
        return dto;
    }

    private String calcInventoryStatus(InventoryDto dto) {
        if (dto == null || dto.getCurrentQty() == null) return "unknown";
        BigDecimal qty = dto.getCurrentQty();
        BigDecimal safety = dto.getSafetyStock() != null ? dto.getSafetyStock() : BigDecimal.ZERO;
        BigDecimal min = dto.getMinStock();
        BigDecimal max = dto.getMaxStock();
        if (min != null && qty.compareTo(min) < 0) return "low";
        if (max != null && qty.compareTo(max) > 0) return "high";
        if (safety.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(safety) < 0) return "warning";
        return "normal";
    }

    private String mapPurchaseStatus(String status) {
        if (status == null) return "draft";
        String s = status.trim().toUpperCase();
        if (Objects.equals(s, "CREATED")) return "draft";
        if (Objects.equals(s, "RECEIVING") || Objects.equals(s, "RECEIVING_STARTED")) return "approved";
        if (Objects.equals(s, "RECEIVED") || Objects.equals(s, "PARTIAL_RECEIVED")) return "processed";
        if (Objects.equals(s, "CANCELLED")) return "closed";
        if (Objects.equals(s, "CLOSED")) return "closed";
        return "draft";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static String str(Map<String, Object> row, String k1, String k2) {
        Object v = row.get(k1);
        if (v == null) v = row.get(k2);
        if (v == null) return "";
        return String.valueOf(v);
    }

    private static String firstNonBlank(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return new BigDecimal(String.valueOf(n));
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}
