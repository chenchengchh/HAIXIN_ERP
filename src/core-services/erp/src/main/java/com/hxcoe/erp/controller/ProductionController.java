package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.mes.MesReportingCreateDTO;
import com.hxcoe.common.dto.mes.MesReportingUpdateDTO;
import com.hxcoe.common.dto.mes.WorkOrderStatusUpdateDTO;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.ApsProductionPlanClient;
import com.hxcoe.erp.client.ApsResourceLoadClient;
import com.hxcoe.erp.client.ApsScheduleDetailClient;
import com.hxcoe.erp.client.ApsScheduleResultClient;
import com.hxcoe.erp.client.ApsSchedulingClient;
import com.hxcoe.erp.client.EamAssetClient;
import com.hxcoe.erp.client.MesReportingClient;
import com.hxcoe.erp.client.MesWorkOrderClient;
import com.hxcoe.erp.dto.production.CapacityDataDto;
import com.hxcoe.erp.dto.production.CapacityPlanningResultDto;
import com.hxcoe.erp.dto.production.EquipmentDto;
import com.hxcoe.erp.dto.production.ProductionLoadDto;
import com.hxcoe.erp.dto.production.ProductionOrderDto;
import com.hxcoe.erp.dto.production.ProductionReportDto;
import com.hxcoe.erp.dto.production.WorkshopOrderDto;
import com.hxcoe.erp.entity.ApsOrderStatusEntity;
import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.repository.ApsOrderStatusRepository;
import com.hxcoe.erp.service.ProductionService;
import com.hxcoe.erp.support.ResultDataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 生产模块控制器
 */
@RestController
@RequestMapping("/api/v1/erp/production")
public class ProductionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionController.class);

    @Autowired
    private ProductionService productionService;

    @Autowired
    private MesWorkOrderClient mesWorkOrderClient;

    @Autowired
    private MesReportingClient mesReportingClient;

    @Autowired
    private ApsResourceLoadClient apsResourceLoadClient;

    @Autowired
    private ApsSchedulingClient apsSchedulingClient;

    @Autowired
    private ApsProductionPlanClient apsProductionPlanClient;

    @Autowired
    private ApsScheduleResultClient apsScheduleResultClient;

    @Autowired
    private ApsScheduleDetailClient apsScheduleDetailClient;

    @Autowired
    private EamAssetClient eamAssetClient;

    @Autowired
    private ApsOrderStatusRepository apsOrderStatusRepository;

    @Autowired
    private com.hxcoe.erp.service.ErpApprovalIntegrationService approvalIntegrationService;

    /**
     * 提交生产订单审批（发起OA统一审批）。
     * <p>向OA提交生产订单审批申请，审批完成后OA回调更新订单状态：
     * 通过→已审核(1)，拒绝→回退草稿(0)。仅待生产(0)状态允许提交。</p>
     *
     * @param id            生产订单ID
     * @param initiatorId   发起人ID（前端传入当前登录用户ID）
     * @param initiatorName 发起人名称（前端传入当前登录用户名称）
     * @return 提交结果
     */
    @PostMapping("/orders/{id}/submit-approval")
    public ApiResponse<Map<String, Object>> submitOrderForApproval(
            @PathVariable Long id,
            @RequestParam(required = false) Long initiatorId,
            @RequestParam(required = false) String initiatorName) {
        ProductionEntity production = productionService.getProductionById(id);
        if (production == null) {
            return notFound("生产订单不存在");
        }
        // 仅待生产(0)状态允许提交审批，防止重复提交
        if (production.getProductionStatus() == null || production.getProductionStatus() != 0) {
            return badRequest("当前状态不允许提交审批");
        }
        boolean submitted = approvalIntegrationService.submitProductionOrderApproval(
                production.getId(), production.getProductionNo(), production.getProductName(),
                production.getProductionQuantity(), production.getWorkshop(),
                initiatorId, initiatorName);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("approvalSubmitted", submitted);
        return success(submitted ? "生产订单已提交审批" : "审批提交失败（OA服务不可用），请稍后重试", data);
    }

    /**
     * 创建生产记录
     *
     * @param productionEntity 生产实体
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<ProductionEntity> createProduction(@RequestBody ProductionEntity productionEntity) {
        ProductionEntity result = productionService.createProduction(productionEntity);
        return success("操作成功", result);
    }

    /**
     * 根据ID查询生产记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductionEntity> getProductionById(@PathVariable Long id) {
        ProductionEntity result = productionService.getProductionById(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("生产记录不存在");
    }

    /**
     * 根据生产单号查询生产记录
     *
     * @param productionNo 生产单号
     * @return 查询结果
     */
    @GetMapping("/no/{productionNo}")
    public ApiResponse<ProductionEntity> getProductionByNo(@PathVariable String productionNo) {
        ProductionEntity result = productionService.getProductionByNo(productionNo);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("生产记录不存在");
    }

    /**
     * 更新生产记录
     *
     * @param productionEntity 生产实体
     * @return 更新结果
     */
    @PutMapping
    public ApiResponse<ProductionEntity> updateProduction(@RequestBody ProductionEntity productionEntity) {
        ProductionEntity result = productionService.updateProduction(productionEntity);
        if (result == null) {
            return notFound("生产记录不存在");
        }
        return success("操作成功", result);
    }

    /**
     * 删除生产记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteProduction(@PathVariable Long id) {
        boolean result = productionService.deleteProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return notFound("生产记录不存在");
    }

    /**
     * 分页查询生产记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param productionNo 生产单号
     * @param productCode 产品编码
     * @param workshop 车间
     * @param productionStatus 生产状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<ProductionEntity>> getProductionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String productionNo,
            @RequestParam(required = false, name = "production_no") String productionNo2,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false, name = "product_code") String productCode2,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false, name = "product_name") String productName2,
            @RequestParam(required = false) String workshop,
            @RequestParam(required = false, name = "production_line") String productionLine,
            @RequestParam(required = false) Integer productionStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        String pn = (productionNo != null && !productionNo.isBlank()) ? productionNo : productionNo2;
        String pc = (productCode != null && !productCode.isBlank()) ? productCode : productCode2;
        String pName = (productName != null && !productName.isBlank()) ? productName : productName2;
        PageResult<ProductionEntity> result = productionService.getProductionList(page, size, pn, pc, pName, workshop, productionLine, productionStatus, startDate, endDate);
        return success("操作成功", result);
    }

    /**
     * 开始生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Boolean> startProduction(@PathVariable Long id) {
        boolean result = productionService.startProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return badRequest("生产开始失败，状态不允许");
    }

    /**
     * 暂停生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/pause")
    public ApiResponse<Boolean> pauseProduction(@PathVariable Long id) {
        boolean result = productionService.pauseProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return badRequest("生产暂停失败，状态不允许");
    }

    /**
     * 恢复生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/resume")
    public ApiResponse<Boolean> resumeProduction(@PathVariable Long id) {
        boolean result = productionService.resumeProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return badRequest("生产恢复失败，状态不允许");
    }

    /**
     * 完成生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<Boolean> completeProduction(@PathVariable Long id) {
        boolean result = productionService.completeProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return badRequest("生产完成失败，状态不允许");
    }

    /**
     * 取消生产
     *
     * @param id 生产记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Boolean> cancelProduction(@PathVariable Long id) {
        boolean result = productionService.cancelProduction(id);
        if (result) {
            return success("操作成功", result);
        }
        return badRequest("生产取消失败，状态不允许");
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<ProductionOrderDto>> orders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "order_no") String orderNo,
            @RequestParam(required = false, name = "product_code") String productCode,
            @RequestParam(required = false, name = "product_name") String productName,
            @RequestParam(required = false) String workshop,
            @RequestParam(required = false, name = "production_line") String productionLine,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source
    ) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            Result<Object> res = apsProductionPlanClient.page(page, size);
            ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
            List<ProductionOrderDto> mapped = new ArrayList<>();
            for (Map<String, Object> row : parts.rows()) {
                ProductionOrderDto dto = new ProductionOrderDto();
                Object idVal = row.get("id");
                if (idVal instanceof Number n) dto.setId(n.longValue());
                else if (idVal != null) {
                    try { dto.setId(Long.parseLong(String.valueOf(idVal))); } catch (Exception ignored) {}
                }
                dto.setOrderNo(String.valueOf(row.getOrDefault("planNo", row.getOrDefault("plan_no", ""))));
                dto.setProductCode(row.get("productCode") != null ? String.valueOf(row.get("productCode")) : String.valueOf(row.getOrDefault("product_code", "")));
                dto.setProductName(row.get("productName") != null ? String.valueOf(row.get("productName")) : String.valueOf(row.getOrDefault("product_name", "")));
                Object qty = row.getOrDefault("quantity", row.get("plannedQty"));
                if (qty instanceof Number n) dto.setPlannedQty(java.math.BigDecimal.valueOf(n.doubleValue()));
                else if (qty != null) {
                    try { dto.setPlannedQty(new java.math.BigDecimal(String.valueOf(qty))); } catch (Exception ignored) {}
                }
                dto.setStartDate(String.valueOf(row.getOrDefault("startTime", row.getOrDefault("start_time", ""))));
                dto.setEndDate(String.valueOf(row.getOrDefault("endTime", row.getOrDefault("end_time", ""))));
                String apsStatus = String.valueOf(row.getOrDefault("status", ""));
                if (dto.getId() != null) {
                    ApsOrderStatusEntity st = apsOrderStatusRepository.findByPlanId(dto.getId()).orElse(null);
                    if (st != null && st.getStatus() != null && !st.getStatus().isBlank()) {
                        apsStatus = st.getStatus();
                    }
                }
                dto.setStatus(apsStatus);
                dto.setSource("aps");
                dto.setCreator("APS");
                dto.setCreateTime(String.valueOf(row.getOrDefault("createdTime", row.getOrDefault("created_time", ""))));
                mapped.add(dto);
            }
            return success("生产订单查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
        }
        Integer productionStatus = mapProductionStatus(status);
        PageResult<ProductionEntity> result = productionService.getProductionList(page, size, orderNo, productCode, productName, workshop, productionLine, productionStatus, null, null);
        List<ProductionOrderDto> mapped = new ArrayList<>();
        for (ProductionEntity e : result.getRecords()) {
            mapped.add(mapProductionEntity(e));
        }
        return success("生产订单查询成功", PageResult.build(result.getTotal(), result.getPageSize(), result.getCurrentPage(), mapped));
    }

    @PostMapping("/orders/{id}/release")
    public ApiResponse<Map<String, Object>> releaseApsOrderToMes(@PathVariable Long id) {
        Result<Object> listRes = apsScheduleResultClient.listByPlan(id);
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(listRes != null ? listRes.getData() : null);
        Long scheduleResultId = null;
        if (!rows.isEmpty()) {
            Object v = rows.get(0).get("id");
            if (v instanceof Number n) scheduleResultId = n.longValue();
            else if (v != null) {
                try { scheduleResultId = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
            }
        }
        if (scheduleResultId == null) {
            return notFound("未找到可下达的排程结果");
        }
        apsScheduleResultClient.release(scheduleResultId);
        upsertApsOrderStatus(id, "RELEASED");
        Map<String, Object> data = new HashMap<>();
        data.put("plan_id", id);
        data.put("schedule_result_id", scheduleResultId);
        data.put("status", "RELEASED");
        return success("下达成功", data);
    }

    @PostMapping("/orders/{id}/start")
    public ApiResponse<Map<String, Object>> startOrder(@PathVariable Long id, @RequestParam(required = false) String source) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            int updated = batchUpdateMesWorkOrdersByPlan(id, "STARTED");
            upsertApsOrderStatus(id, "STARTED");
            Map<String, Object> data = new HashMap<>();
            data.put("plan_id", id);
            data.put("updated_work_orders", updated);
            data.put("status", "STARTED");
            return success("操作成功", data);
        }
        boolean result = productionService.startProduction(id);
        if (result) return success("操作成功", Map.of("id", id, "status", "in_production"));
        return badRequest("生产开始失败，状态不允许");
    }

    @PostMapping("/orders/{id}/pause")
    public ApiResponse<Map<String, Object>> pauseOrder(@PathVariable Long id, @RequestParam(required = false) String source) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            int updated = batchUpdateMesWorkOrdersByPlan(id, "PAUSED");
            upsertApsOrderStatus(id, "PAUSED");
            Map<String, Object> data = new HashMap<>();
            data.put("plan_id", id);
            data.put("updated_work_orders", updated);
            data.put("status", "PAUSED");
            return success("操作成功", data);
        }
        boolean result = productionService.pauseProduction(id);
        if (result) return success("操作成功", Map.of("id", id, "status", "paused"));
        return badRequest("生产暂停失败，状态不允许");
    }

    @PostMapping("/orders/{id}/resume")
    public ApiResponse<Map<String, Object>> resumeOrder(@PathVariable Long id, @RequestParam(required = false) String source) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            int updated = batchUpdateMesWorkOrdersByPlan(id, "STARTED");
            upsertApsOrderStatus(id, "STARTED");
            Map<String, Object> data = new HashMap<>();
            data.put("plan_id", id);
            data.put("updated_work_orders", updated);
            data.put("status", "STARTED");
            return success("操作成功", data);
        }
        boolean result = productionService.resumeProduction(id);
        if (result) return success("操作成功", Map.of("id", id, "status", "in_production"));
        return badRequest("生产恢复失败，状态不允许");
    }

    @PostMapping("/orders/{id}/complete")
    public ApiResponse<Map<String, Object>> completeOrder(@PathVariable Long id, @RequestParam(required = false) String source) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            int updated = batchUpdateMesWorkOrdersByPlan(id, "COMPLETED");
            upsertApsOrderStatus(id, "COMPLETED");
            Map<String, Object> data = new HashMap<>();
            data.put("plan_id", id);
            data.put("updated_work_orders", updated);
            data.put("status", "COMPLETED");
            return success("操作成功", data);
        }
        boolean result = productionService.completeProduction(id);
        if (result) return success("操作成功", Map.of("id", id, "status", "completed"));
        return badRequest("生产完成失败，状态不允许");
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancelOrder(@PathVariable Long id, @RequestParam(required = false) String source) {
        if (source != null && "aps".equalsIgnoreCase(source)) {
            int updated = batchUpdateMesWorkOrdersByPlan(id, "CANCELLED");
            upsertApsOrderStatus(id, "CANCELLED");
            Map<String, Object> data = new HashMap<>();
            data.put("plan_id", id);
            data.put("updated_work_orders", updated);
            data.put("status", "CANCELLED");
            return success("操作成功", data);
        }
        boolean result = productionService.cancelProduction(id);
        if (result) return success("操作成功", Map.of("id", id, "status", "cancelled"));
        return badRequest("生产取消失败，状态不允许");
    }

    private void upsertApsOrderStatus(Long planId, String status) {
        ApsOrderStatusEntity existing = apsOrderStatusRepository.findByPlanId(planId).orElse(null);
        if (existing == null) {
            existing = new ApsOrderStatusEntity();
            existing.setPlanId(planId);
        }
        existing.setStatus(status);
        existing.setUpdatedTime(LocalDateTime.now());
        apsOrderStatusRepository.save(existing);
    }

    private int batchUpdateMesWorkOrdersByPlan(Long planId, String status) {
        Result<Object> detailRes = apsScheduleDetailClient.listByPlan(planId);
        List<Map<String, Object>> details = ResultDataExtractor.asListOfMap(detailRes != null ? detailRes.getData() : null);
        int updated = 0;
        for (Map<String, Object> d : details) {
            Object idVal = d.get("id");
            Long detailId = null;
            if (idVal instanceof Number n) detailId = n.longValue();
            else if (idVal != null) {
                try { detailId = Long.parseLong(String.valueOf(idVal)); } catch (Exception ignored) {}
            }
            if (detailId == null) continue;
            String workOrderNo = "WO-" + detailId;
            // B3 修复：使用强类型 DTO 替代弱类型 Map，明确接口契约
            WorkOrderStatusUpdateDTO statusBody = new WorkOrderStatusUpdateDTO();
            statusBody.setStatus(status);
            mesWorkOrderClient.updateStatusByNo(workOrderNo, statusBody);
            updated++;
        }
        return updated;
    }

    @PostMapping("/orders")
    public ApiResponse<ProductionOrderDto> createOrder(@RequestBody Map<String, Object> body) {
        ProductionEntity entity = new ProductionEntity();
        applyProductionBody(entity, body);
        ProductionEntity created = productionService.createProduction(entity);
        return success("生产订单创建成功", mapProductionEntity(created));
    }

    @PutMapping("/orders/{id}")
    public ApiResponse<ProductionOrderDto> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ProductionEntity existing = productionService.getProductionById(id);
        if (existing == null) {
            return notFound("生产订单不存在");
        }
        applyProductionBody(existing, body);
        ProductionEntity updated = productionService.updateProduction(existing);
        return success("生产订单更新成功", mapProductionEntity(updated));
    }

    @DeleteMapping("/orders/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        boolean deleted = productionService.deleteProduction(id);
        if (!deleted) {
            return notFound("生产订单不存在");
        }
        return success("删除成功", null);
    }

    @GetMapping("/workshop-orders")
    public ApiResponse<PageResult<WorkshopOrderDto>> workshopOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Result<Object> res = mesWorkOrderClient.list();
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        int p = Math.max(page, 1);
        int s = Math.max(size, 1);
        int from = Math.min((p - 1) * s, rows.size());
        int to = Math.min(from + s, rows.size());
        List<WorkshopOrderDto> mapped = new ArrayList<>();
        for (int i = from; i < to; i++) {
            mapped.add(mapWorkOrder(rows.get(i)));
        }
        return success("工序工单查询成功", PageResult.build((long) rows.size(), s, p, mapped));
    }

    @PostMapping("/workshop-orders")
    public ApiResponse<Object> createWorkshopOrder(@RequestBody Map<String, Object> body) {
        Result<Object> res = mesWorkOrderClient.create(body);
        return success("工序工单创建成功", res != null ? res.getData() : null);
    }

    @GetMapping("/reports")
    public ApiResponse<PageResult<ProductionReportDto>> reports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String reportNo,
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Result<Object> res = mesReportingClient.list(page, size, reportNo, workOrderNo, operatorName, status, startDate, endDate);
        ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(res != null ? res.getData() : null);
        List<ProductionReportDto> mapped = new ArrayList<>();
        for (Map<String, Object> row : parts.rows()) {
            mapped.add(mapReport(row));
        }
        return success("生产报工查询成功", PageResult.build(parts.total(), parts.size(), parts.page(), mapped));
    }

    @PostMapping("/reports")
    public ApiResponse<Object> createReport(@RequestBody Map<String, Object> body) {
        // B3 修复：Map → 强类型 MesReportingCreateDTO，明确跨服务契约
        MesReportingCreateDTO dto = toMesReportingCreateDTO(body);
        Result<Object> res = mesReportingClient.manual(dto);
        return success("生产报工创建成功", res != null ? res.getData() : null);
    }

    @PutMapping("/reports/{id}")
    public ApiResponse<Object> updateReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // B3 修复：Map → 强类型 MesReportingUpdateDTO，明确跨服务契约
        MesReportingUpdateDTO dto = toMesReportingUpdateDTO(body);
        Result<Object> res = mesReportingClient.update(id, dto);
        return success("生产报工更新成功", res != null ? res.getData() : null);
    }

    /**
     * 将前端传入的 Map 转换为 MES 报工创建强类型 DTO（B3 契约对齐）。
     *
     * @param body 前端请求体
     * @return 报工创建 DTO
     */
    private MesReportingCreateDTO toMesReportingCreateDTO(Map<String, Object> body) {
        MesReportingCreateDTO dto = new MesReportingCreateDTO();
        if (body == null) {
            return dto;
        }
        dto.setReportNo(asString(body.get("reportNo")));
        dto.setWorkOrderNo(asString(body.get("workOrderNo")));
        dto.setErpProductionNo(asString(body.get("erpProductionNo")));
        dto.setStepName(asString(body.get("stepName")));
        dto.setWorkstationName(asString(body.get("workstationName")));
        dto.setOperatorName(asString(body.get("operatorName")));
        dto.setStartTime(asLocalDateTime(body.get("startTime")));
        dto.setEndTime(asLocalDateTime(body.get("endTime")));
        dto.setGoodQty(asInteger(body.get("goodQty")));
        dto.setScrapQty(asInteger(body.get("scrapQty")));
        dto.setReworkQty(asInteger(body.get("reworkQty")));
        dto.setWorkingHours(asDouble(body.get("workingHours")));
        dto.setMachineHours(asDouble(body.get("machineHours")));
        dto.setStatus(asString(body.get("status")));
        dto.setRemark(asString(body.get("remark")));
        return dto;
    }

    /**
     * 将前端传入的 Map 转换为 MES 报工更新强类型 DTO（B3 契约对齐）。
     *
     * @param body 前端请求体
     * @return 报工更新 DTO
     */
    private MesReportingUpdateDTO toMesReportingUpdateDTO(Map<String, Object> body) {
        MesReportingUpdateDTO dto = new MesReportingUpdateDTO();
        if (body == null) {
            return dto;
        }
        dto.setReportNo(asString(body.get("reportNo")));
        dto.setWorkOrderNo(asString(body.get("workOrderNo")));
        dto.setOperatorName(asString(body.get("operatorName")));
        dto.setEndTime(asLocalDateTime(body.get("endTime")));
        dto.setGoodQty(asInteger(body.get("goodQty")));
        dto.setScrapQty(asInteger(body.get("scrapQty")));
        dto.setReworkQty(asInteger(body.get("reworkQty")));
        dto.setWorkingHours(asDouble(body.get("workingHours")));
        dto.setMachineHours(asDouble(body.get("machineHours")));
        dto.setStatus(asString(body.get("status")));
        dto.setRemark(asString(body.get("remark")));
        return dto;
    }

    /**
     * 安全转字符串（null/空安全）。
     */
    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    /**
     * 安全转 Integer。
     */
    private static Integer asInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v).trim()); } catch (Exception e) { return null; }
    }

    /**
     * 安全转 Double。
     */
    private static Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v).trim()); } catch (Exception e) { return null; }
    }

    /**
     * 安全转 LocalDateTime（兼容 ISO 与空格分隔格式）。
     */
    private static LocalDateTime asLocalDateTime(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return null;
        try {
            if (s.contains("T")) {
                return LocalDateTime.parse(s.length() > 19 ? s.substring(0, 19) : s);
            }
            if (s.length() >= 19) {
                return LocalDateTime.parse(s.replace(' ', 'T'));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @GetMapping("/load")
    public ApiResponse<PageResult<ProductionLoadDto>> load(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String timeScale
    ) {
        Result<Object> res = apsResourceLoadClient.list(planId, null, startTime, endTime, timeScale);
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        int p = Math.max(page, 1);
        int s = Math.max(size, 1);
        int from = Math.min((p - 1) * s, rows.size());
        int to = Math.min(from + s, rows.size());
        List<ProductionLoadDto> mapped = new ArrayList<>();
        for (int i = from; i < to; i++) {
            mapped.add(mapLoad(rows.get(i)));
        }
        return success("生产负荷查询成功", PageResult.build((long) rows.size(), s, p, mapped));
    }

    @GetMapping("/equipment")
    public ApiResponse<PageResult<EquipmentDto>> equipment(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Result<Object> res = eamAssetClient.list();
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        int p = Math.max(page, 1);
        int s = Math.max(size, 1);
        int from = Math.min((p - 1) * s, rows.size());
        int to = Math.min(from + s, rows.size());
        List<EquipmentDto> mapped = new ArrayList<>();
        for (int i = from; i < to; i++) {
            mapped.add(mapAsset(rows.get(i)));
        }
        return success("设备列表查询成功", PageResult.build((long) rows.size(), s, p, mapped));
    }

    @GetMapping("/equipment/status")
    public ApiResponse<Map<String, Object>> equipmentStatus() {
        Result<Object> res = eamAssetClient.list();
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        Map<String, Object> data = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String st = row.get("status") != null ? String.valueOf(row.get("status")) : "unknown";
            counts.put(st, counts.getOrDefault(st, 0) + 1);
        }
        Map<String, Integer> statusOverview = new HashMap<>();
        statusOverview.put("online", counts.getOrDefault("online", 0));
        statusOverview.put("offline", counts.getOrDefault("offline", 0));
        statusOverview.put("running", counts.getOrDefault("running", 0));
        statusOverview.put("stopped", counts.getOrDefault("stopped", 0));
        data.put("counts", counts);
        data.put("status_overview", statusOverview);
        data.put("total", rows.size());
        return success("设备状态查询成功", data);
    }

    @PostMapping("/capacity-planning/run")
    public ApiResponse<PageResult<CapacityPlanningResultDto>> runCapacityPlanning(@RequestBody Map<String, Object> body) {
        apsSchedulingClient.execute(body);
        PageResult<CapacityPlanningResultDto> result = productionService.runCapacityPlanning(body);
        return success("产能规划执行成功", result);
    }

    @GetMapping("/capacity-planning/results")
    public ApiResponse<PageResult<CapacityPlanningResultDto>> capacityPlanningResults(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return success("产能规划结果查询成功", productionService.getCapacityPlanningResults(page, size));
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

    private ProductionOrderDto mapProductionEntity(ProductionEntity e) {
        ProductionOrderDto dto = new ProductionOrderDto();
        if (e == null) return dto;
        dto.setId(e.getId());
        dto.setOrderNo(e.getProductionNo());
        dto.setSource("local");
        dto.setProductCode(e.getProductCode());
        dto.setProductName(e.getProductName());
        dto.setPlannedQty(BigDecimal.valueOf(e.getProductionQuantity() == null ? 0 : e.getProductionQuantity()));
        dto.setActualQty(BigDecimal.valueOf(e.getCompletedQuantity() == null ? 0 : e.getCompletedQuantity()));
        dto.setStartDate(e.getPlanStartTime() != null ? String.valueOf(e.getPlanStartTime()) : null);
        dto.setEndDate(e.getPlanEndTime() != null ? String.valueOf(e.getPlanEndTime()) : null);
        dto.setWorkshop(e.getWorkshop());
        dto.setProductionLine(e.getProductionLine());
        dto.setStatus(mapProductionStatusToString(e.getProductionStatus()));
        // 映射 MES 集成状态（F1 前端配套）
        dto.setMesIntegrationStatus(e.getMesIntegrationStatus());
        dto.setPriority(e.getPriority() != null ? String.valueOf(e.getPriority()) : null);
        dto.setRemark(e.getRemark());
        dto.setCreator(e.getCreatedBy());
        dto.setCreateTime(e.getCreatedTime() != null ? String.valueOf(e.getCreatedTime()) : null);
        return dto;
    }

    private void applyProductionBody(ProductionEntity e, Map<String, Object> body) {
        if (e == null || body == null) return;
        if (body.get("order_no") != null) {
            String v = String.valueOf(body.get("order_no"));
            if (!v.isBlank()) e.setProductionNo(v);
        }
        if (body.get("product_code") != null) e.setProductCode(String.valueOf(body.get("product_code")));
        if (body.get("product_name") != null) e.setProductName(String.valueOf(body.get("product_name")));
        if (body.get("workshop") != null) e.setWorkshop(String.valueOf(body.get("workshop")));
        if (body.get("production_line") != null) e.setProductionLine(String.valueOf(body.get("production_line")));
        if (body.get("remark") != null) e.setRemark(String.valueOf(body.get("remark")));
        if (body.get("priority") != null) {
            Object p = body.get("priority");
            if (p instanceof Number n) e.setPriority(n.intValue());
            else {
                try { e.setPriority(Integer.parseInt(String.valueOf(p))); } catch (Exception ignore) { }
            }
        }
        if (body.get("planned_qty") != null) {
            Object qty = body.get("planned_qty");
            if (qty instanceof Number n) e.setProductionQuantity(n.intValue());
            else {
                try { e.setProductionQuantity(Integer.parseInt(String.valueOf(qty))); } catch (Exception ignore) { }
            }
        }
        if (body.get("start_date") != null) {
            LocalDateTime dt = parseDateTime(body.get("start_date"));
            if (dt != null) e.setPlanStartTime(dt);
        }
        if (body.get("end_date") != null) {
            LocalDateTime dt = parseDateTime(body.get("end_date"));
            if (dt != null) e.setPlanEndTime(dt);
        }
        if (body.get("status") != null) {
            e.setProductionStatus(mapProductionStatus(String.valueOf(body.get("status"))));
        }
    }

    private Integer mapProductionStatus(String status) {
        if (status == null) return null;
        String s = status.trim().toLowerCase();
        if (Objects.equals(s, "draft")) return 0;
        if (Objects.equals(s, "approved")) return 1;
        if (Objects.equals(s, "in_production")) return 2;
        if (Objects.equals(s, "completed")) return 3;
        if (Objects.equals(s, "cancelled")) return 5;
        return null;
    }

    private String mapProductionStatusToString(Integer status) {
        if (status == null) return "draft";
        return switch (status) {
            case 0 -> "draft";
            case 1 -> "approved";
            case 2 -> "in_production";
            case 3 -> "completed";
            case 4 -> "in_production";
            case 5 -> "cancelled";
            default -> "draft";
        };
    }

    private static LocalDateTime parseDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) {
            try {
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(n.longValue()), java.time.ZoneId.systemDefault());
            } catch (Exception ignore) {
            }
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignore) {
        }
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignore) {
        }
        try {
            LocalDate d = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return LocalDateTime.of(d, LocalTime.MIN);
        } catch (DateTimeParseException ignore) {
        }
        return null;
    }

    private WorkshopOrderDto mapWorkOrder(Map<String, Object> row) {
        WorkshopOrderDto dto = new WorkshopOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setOrderNo(str(row, "workOrderNo", "work_order_no"));
        dto.setProductionOrderNo(str(row, "sourceId", "source_id"));
        dto.setProcessName(str(row, "processName", "process_name"));
        dto.setWorkstation(str(row, "workCenterName", "work_center_name"));
        dto.setPlannedQty(toDecimal(row.get("plannedQuantity")));
        dto.setCompletedQty(toDecimal(row.get("actualQuantity")));
        dto.setStartTime(str(row, "plannedStartTime", "planned_start_time"));
        dto.setEndTime(str(row, "plannedEndTime", "planned_end_time"));
        dto.setStatus(str(row, "status", "status"));
        dto.setOperator(str(row, "operator", "operator"));
        dto.setRemark(str(row, "remark", "remark"));
        return dto;
    }

    private ProductionReportDto mapReport(Map<String, Object> row) {
        ProductionReportDto dto = new ProductionReportDto();
        dto.setId(toLong(row.get("id")));
        dto.setReportNo(str(row, "reportNo", "report_no"));
        dto.setWorkshopOrderNo(str(row, "workOrderNo", "work_order_no"));
        dto.setProductionOrderNo(str(row, "productionOrderNo", "production_order_no"));
        dto.setProductCode(str(row, "materialCode", "material_code"));
        dto.setProductName(str(row, "materialName", "material_name"));
        dto.setReportDate(str(row, "reportDate", "report_date"));
        dto.setReportedQty(toDecimal(row.get("reportedQty")));
        dto.setQualifiedQty(toDecimal(row.get("qualifiedQty")));
        dto.setDefectiveQty(toDecimal(row.get("defectiveQty")));
        dto.setOperator(str(row, "operatorName", "operator_name"));
        dto.setStatus(str(row, "status", "status"));
        dto.setCreateTime(str(row, "createTime", "create_time"));
        dto.setRemark(str(row, "remark", "remark"));
        return dto;
    }

    private ProductionLoadDto mapLoad(Map<String, Object> row) {
        ProductionLoadDto dto = new ProductionLoadDto();
        dto.setId(toLong(row.get("id")));
        String departmentName = str(row, "departmentName", "department_name");
        String workCenterName = str(row, "workCenterName", "work_center_name");
        String period = str(row, "timePeriod", "time_period");
        String startTime = str(row, "startTime", "start_time");
        BigDecimal used = toDecimal(row.get("usedCapacity"));
        if (used == null) used = toDecimal(row.get("used_capacity"));
        BigDecimal total = toDecimal(row.get("totalCapacity"));
        if (total == null) total = toDecimal(row.get("total_capacity"));
        BigDecimal available = toDecimal(row.get("availableCapacity"));
        if (available == null) available = toDecimal(row.get("available_capacity"));

        if (departmentName.isBlank()) departmentName = "生产部";
        if (workCenterName.isBlank()) workCenterName = str(row, "resourceName", "resource_name");

        dto.setWorkshop(departmentName);
        dto.setProductionLine(workCenterName);
        dto.setDate(str(row, "date", "date"));
        dto.setPlannedLoad(used);
        dto.setActualLoad(used);
        dto.setCapacityUtilization(toDecimal(row.get("loadRate")));
        dto.setRemark(str(row, "remark", "remark"));

        dto.setDepartmentName(departmentName);
        dto.setWorkCenterName(workCenterName);
        dto.setProductName(str(row, "productName", "product_name"));
        if (dto.getProductName() == null || dto.getProductName().isBlank()) dto.setProductName("");
        dto.setAvailableCapacity(total != null ? total : available);
        BigDecimal rate = toDecimal(row.get("loadRate"));
        if (rate == null) rate = toDecimal(row.get("load_rate"));
        if (rate != null && rate.compareTo(BigDecimal.ONE) <= 0) rate = rate.multiply(new BigDecimal("100"));
        dto.setLoadRate(rate);
        dto.setLoadStatus(calcLoadStatus(rate));
        dto.setPeriod(period);
        dto.setPeriodDate(extractDate(startTime));
        return dto;
    }

    private EquipmentDto mapAsset(Map<String, Object> row) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(toLong(row.get("id")));
        dto.setEquipmentCode(str(row, "code", "code"));
        dto.setEquipmentName(str(row, "name", "name"));
        dto.setDepartmentName(str(row, "departmentName", "department_name"));
        dto.setWorkCenterName(str(row, "workCenterName", "work_center_name"));
        dto.setModel(str(row, "model", "model"));
        Object c = row.get("capacity");
        if (c instanceof Number n) dto.setCapacity(n.intValue());
        else dto.setCapacity(null);
        dto.setLastMaintainDate(str(row, "lastMaintainDate", "last_maintain_date"));
        dto.setEquipmentType(str(row, "categoryName", "category_name"));
        dto.setLocation(str(row, "location", "location"));
        dto.setStatus(str(row, "status", "status"));
        dto.setRemark(str(row, "model", "model"));
        return dto;
    }

    private static String calcLoadStatus(BigDecimal rate) {
        if (rate == null) return "normal";
        if (rate.compareTo(new BigDecimal("60")) < 0) return "low";
        if (rate.compareTo(new BigDecimal("85")) < 0) return "normal";
        return "high";
    }

    private static String extractDate(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.isBlank()) return "";
        int t = s.indexOf('T');
        if (t > 0) return s.substring(0, t);
        int sp = s.indexOf(' ');
        if (sp > 0) return s.substring(0, sp);
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
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

    /**
     * 测试生产模块接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    public ApiResponse<String> testProduction() {
        return success("操作成功", "Production module is working");
    }

    /**
     * 获取产能数据
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param departmentName 部门名称
     * @param workCenterName 工作中心名称
     * @param resourceName 资源名称
     * @param period 周期类型(day/week/month)
     * @return 产能数据列表
     */
    @GetMapping("/capacity")
    public ApiResponse<PageResult<CapacityDataDto>> getCapacity(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(name = "department_name", required = false) String departmentNameSnake,
            @RequestParam(required = false) String workCenterName,
            @RequestParam(name = "work_center_name", required = false) String workCenterNameSnake,
            @RequestParam(required = false) String resourceName,
            @RequestParam(name = "resource_name", required = false) String resourceNameSnake,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(name = "start_date", required = false) String startDateSnake,
            @RequestParam(required = false) String endDate,
            @RequestParam(name = "end_date", required = false) String endDateSnake
    ) {
        String dep = (departmentName != null && !departmentName.isBlank()) ? departmentName : departmentNameSnake;
        String wc = (workCenterName != null && !workCenterName.isBlank()) ? workCenterName : workCenterNameSnake;
        String rn = (resourceName != null && !resourceName.isBlank()) ? resourceName : resourceNameSnake;
        String sd = (startDate != null && !startDate.isBlank()) ? startDate : startDateSnake;
        String ed = (endDate != null && !endDate.isBlank()) ? endDate : endDateSnake;

        logger.info("获取产能数据: page={}, size={}, departmentName={}, workCenterName={}, resourceName={}, period={}, startDate={}, endDate={}",
                page, size, dep, wc, rn, period, sd, ed);
        PageResult<CapacityDataDto> result = productionService.getCapacity(page, size, dep, wc, rn, period, sd, ed);
        return success("获取产能数据成功", result);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateProductionStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) Double actualQuantity) {
        productionService.updateStatus(id, status, actualQuantity);
        return success("操作成功", null);
    }

    /**
     * 按生产单号更新生产状态（B4 修复：MES 回写 ERP 使用业务键）。
     *
     * <p>MES 工单开工/完工时通过 erpProductionNo 回写 ERP 生产单状态，
     * 避免使用易变的 MES 工单主键误传给 ERP。
     *
     * @param productionNo    生产单号（业务键）
     * @param status          外部状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量（完工时回填，可空）
     * @return 操作结果
     */
    @PutMapping("/no/{productionNo}/status")
    public ApiResponse<Void> updateProductionStatusByNo(@PathVariable String productionNo,
                                                       @RequestParam String status,
                                                       @RequestParam(required = false) Double actualQuantity) {
        boolean ok = productionService.updateStatusByNo(productionNo, status, actualQuantity);
        if (!ok) {
            return ApiResponse.error(404, "生产单不存在或状态不支持: " + productionNo);
        }
        return success("操作成功", null);
    }
}
