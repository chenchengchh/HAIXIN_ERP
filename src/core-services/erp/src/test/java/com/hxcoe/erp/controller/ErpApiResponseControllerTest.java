package com.hxcoe.erp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.client.ApsResourceLoadClient;
import com.hxcoe.erp.client.ApsScheduleResultClient;
import com.hxcoe.erp.client.ApsSchedulingClient;
import com.hxcoe.erp.client.EamAssetClient;
import com.hxcoe.erp.client.MesReportingClient;
import com.hxcoe.erp.client.MesWorkOrderClient;
import com.hxcoe.erp.dto.production.CapacityDataDto;
import com.hxcoe.erp.dto.production.CapacityPlanningResultDto;
import com.hxcoe.erp.dto.production.EquipmentDto;
import com.hxcoe.erp.dto.production.ProductionLoadDto;
import com.hxcoe.erp.dto.production.ProductionReportDto;
import com.hxcoe.erp.dto.production.WorkshopOrderDto;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.entity.SupplyChainEntity;
import com.hxcoe.erp.client.WmsInboundAsnClient;
import com.hxcoe.erp.model.OrderRelationModel;
import com.hxcoe.erp.repository.ApsOrderStatusRepository;
import com.hxcoe.erp.repository.FinanceTaskRepository;
import com.hxcoe.erp.service.ErpDataQualityService;
import com.hxcoe.erp.service.FinanceTaskProcessorService;
import com.hxcoe.erp.service.OrderRelationService;
import com.hxcoe.erp.service.ProductionService;
import com.hxcoe.erp.service.SupplyChainService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ErpApiResponseControllerTest {

    @Test
    void financeTaskAdminShouldReturn404WhenTaskMissing() {
        FinanceTaskRepository repository = mock(FinanceTaskRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FinanceTaskAdminController controller = new FinanceTaskAdminController();
        ReflectionTestUtils.setField(controller, "financeTaskRepository", repository);
        ReflectionTestUtils.setField(controller, "financeTaskProcessorService", mock(FinanceTaskProcessorService.class));

        ApiResponse<FinanceTaskEntity> response = controller.getById(1L);

        assertEquals(404, response.getCode());
        assertEquals("未找到", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void orderRelationShouldReturn404WhenEntityMissing() {
        OrderRelationService service = mock(OrderRelationService.class);
        when(service.getOrderRelationById("SO-1")).thenReturn(null);

        OrderRelationController controller = new OrderRelationController();
        ReflectionTestUtils.setField(controller, "orderRelationService", service);

        ApiResponse<OrderRelationModel> response = controller.getOrderRelationById("SO-1");

        assertEquals(404, response.getCode());
        assertEquals("订单关联不存在", response.getMsg());
    }

    @Test
    void dataQualityResolveShouldReturn400WhenResolveFails() {
        ErpDataQualityService service = mock(ErpDataQualityService.class);
        when(service.resolveTask(9L, "manual", "RESOLVED")).thenReturn(false);

        DataQualityAdminController controller = new DataQualityAdminController();
        ReflectionTestUtils.setField(controller, "erpDataQualityService", service);

        ApiResponse<Boolean> response = controller.resolve(9L, Map.of("resolution", "manual", "status", "RESOLVED"));

        assertEquals(400, response.getCode());
        assertEquals("处理失败", response.getMsg());
    }

    @Test
    void dataQualityTasksShouldReturnApiResponsePage() {
        ErpDataQualityService service = mock(ErpDataQualityService.class);
        PageResult<Map<String, Object>> page = PageResult.build(1L, 10, 1, List.of(Map.of("id", 1L)));
        when(service.listTasks(1, 10, null)).thenReturn(page);

        DataQualityAdminController controller = new DataQualityAdminController();
        ReflectionTestUtils.setField(controller, "erpDataQualityService", service);

        ApiResponse<PageResult<Map<String, Object>>> response = controller.tasks(1, 10, null);

        assertEquals(200, response.getCode());
        assertTrue(response.getData() != null);
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void supplyChainShouldReturn404WhenEntityMissing() {
        SupplyChainService service = mock(SupplyChainService.class);
        when(service.getSupplyChainById(8L)).thenReturn(null);

        SupplyChainController controller = new SupplyChainController();
        ReflectionTestUtils.setField(controller, "supplyChainService", service);

        ApiResponse<SupplyChainEntity> response = controller.getSupplyChainById(8L);

        assertEquals(404, response.getCode());
        assertEquals("供应链记录不存在", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void supplyChainReceiptShouldReturn400WhenPurchaseOrderIdMissing() {
        SupplyChainController controller = new SupplyChainController();

        ApiResponse<Object> response = controller.receipt(Map.of());

        assertEquals(400, response.getCode());
        assertEquals("purchase_order_id 不能为空", response.getMsg());
    }

    @Test
    void supplyChainPurchaseOrderShouldReturn404WhenDownstreamDetailMissing() {
        WmsInboundAsnClient client = mock(WmsInboundAsnClient.class);
        when(client.detail(100L)).thenReturn(Result.<Object>success(new HashMap<String, Object>()));

        SupplyChainController controller = new SupplyChainController();
        ReflectionTestUtils.setField(controller, "wmsInboundAsnClient", client);

        ApiResponse<?> response = controller.getPurchaseOrder(100L);

        assertEquals(404, response.getCode());
        assertEquals("采购订单不存在", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void supplyChainSalesDeliveryShouldReturn400WhenWmsCreateFails() {
        WmsOutboundOrderClient client = mock(WmsOutboundOrderClient.class);
        when(client.create(any())).thenReturn(Result.error(500, "WMS创建失败"));

        SupplyChainController controller = new SupplyChainController();
        ReflectionTestUtils.setField(controller, "wmsOutboundOrderClient", client);

        ApiResponse<Object> response = controller.createSalesDelivery(Map.of("warehouseCode", "WH-001", "items", List.of()));

        assertEquals(400, response.getCode());
        assertEquals("WMS创建失败", response.getMsg());
    }

    @Test
    void productionUpdateOrderShouldReturn404WhenOrderMissing() {
        ProductionService service = mock(ProductionService.class);
        when(service.getProductionById(33L)).thenReturn(null);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<?> response = controller.updateOrder(33L, Map.of("remark", "updated"));

        assertEquals(404, response.getCode());
        assertEquals("生产订单不存在", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionStartOrderShouldReturn400WhenTransitionFails() {
        ProductionService service = mock(ProductionService.class);
        when(service.startProduction(12L)).thenReturn(false);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<Map<String, Object>> response = controller.startOrder(12L, null);

        assertEquals(400, response.getCode());
        assertEquals("生产开始失败，状态不允许", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionReleaseShouldReturn404WhenNoScheduleResultFound() {
        ApsScheduleResultClient scheduleResultClient = mock(ApsScheduleResultClient.class);
        when(scheduleResultClient.listByPlan(21L)).thenReturn(Result.success(List.of()));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "apsScheduleResultClient", scheduleResultClient);

        ApiResponse<Map<String, Object>> response = controller.releaseApsOrderToMes(21L);

        assertEquals(404, response.getCode());
        assertEquals("未找到可下达的排程结果", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionCreateOrderShouldReturnApiResponseWhenCreated() {
        ProductionService service = mock(ProductionService.class);
        ProductionEntity entity = new ProductionEntity();
        entity.setId(55L);
        entity.setProductionNo("MO-55");
        entity.setProductCode("P-01");
        entity.setProductName("产品A");
        entity.setProductionQuantity(10);
        entity.setCompletedQuantity(0);
        entity.setProductionStatus(0);
        when(service.createProduction(org.mockito.ArgumentMatchers.any(ProductionEntity.class))).thenReturn(entity);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<?> response = controller.createOrder(Map.of(
                "order_no", "MO-55",
                "product_code", "P-01",
                "product_name", "产品A",
                "planned_qty", 10
        ));

        assertEquals(200, response.getCode());
        assertEquals("生产订单创建成功", response.getMsg());
    }

    @Test
    void productionDeleteOrderShouldReturn404WhenDeleteMisses() {
        ProductionService service = mock(ProductionService.class);
        when(service.deleteProduction(88L)).thenReturn(false);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<Void> response = controller.deleteOrder(88L);

        assertEquals(404, response.getCode());
        assertEquals("生产订单不存在", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionLegacyGetByIdShouldReturn404WhenMissing() {
        ProductionService service = mock(ProductionService.class);
        when(service.getProductionById(101L)).thenReturn(null);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<ProductionEntity> response = controller.getProductionById(101L);

        assertEquals(404, response.getCode());
        assertEquals("生产记录不存在", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionLegacyPauseShouldReturn400WhenTransitionFails() {
        ProductionService service = mock(ProductionService.class);
        when(service.pauseProduction(8L)).thenReturn(false);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<Boolean> response = controller.pauseProduction(8L);

        assertEquals(400, response.getCode());
        assertEquals("生产暂停失败，状态不允许", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionLegacyUpdateStatusShouldReturn200() {
        ProductionService service = mock(ProductionService.class);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<Void> response = controller.updateProductionStatus(5L, "COMPLETED", 12.5);

        assertEquals(200, response.getCode());
        assertEquals("操作成功", response.getMsg());
        assertNull(response.getData());
    }

    @Test
    void productionWorkshopOrdersShouldReturnApiResponsePage() {
        MesWorkOrderClient client = mock(MesWorkOrderClient.class);
        when(client.list()).thenReturn(Result.success(List.of(
                Map.of(
                        "id", 1L,
                        "workOrderNo", "WO-1",
                        "sourceId", "MO-1",
                        "processName", "装配",
                        "workCenterName", "产线A",
                        "status", "CREATED"
                )
        )));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "mesWorkOrderClient", client);

        ApiResponse<PageResult<WorkshopOrderDto>> response = controller.workshopOrders(1, 10);

        assertEquals(200, response.getCode());
        assertEquals("工序工单查询成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionCreateWorkshopOrderShouldReturnApiResponse() {
        MesWorkOrderClient client = mock(MesWorkOrderClient.class);
        when(client.create(Map.of("workOrderNo", "WO-2"))).thenReturn(Result.success(Map.of("workOrderNo", "WO-2")));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "mesWorkOrderClient", client);

        ApiResponse<Object> response = controller.createWorkshopOrder(Map.of("workOrderNo", "WO-2"));

        assertEquals(200, response.getCode());
        assertEquals("工序工单创建成功", response.getMsg());
    }

    @Test
    void productionReportsShouldReturnApiResponsePage() {
        MesReportingClient client = mock(MesReportingClient.class);
        when(client.list(1, 10, null, null, null, null, null, null)).thenReturn(Result.success(Map.of(
                "list", List.of(Map.of(
                        "id", 9L,
                        "reportNo", "R-1",
                        "workOrderNo", "WO-1",
                        "status", "DONE"
                )),
                "total", 1,
                "page", 1,
                "pageSize", 10
        )));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "mesReportingClient", client);

        ApiResponse<PageResult<ProductionReportDto>> response = controller.reports(1, 10, null, null, null, null, null, null);

        assertEquals(200, response.getCode());
        assertEquals("生产报工查询成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionUpdateReportShouldReturnApiResponse() {
        MesReportingClient client = mock(MesReportingClient.class);
        when(client.update(5L, Map.of("status", "DONE"))).thenReturn(Result.success(Map.of("id", 5L, "status", "DONE")));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "mesReportingClient", client);

        ApiResponse<Object> response = controller.updateReport(5L, Map.of("status", "DONE"));

        assertEquals(200, response.getCode());
        assertEquals("生产报工更新成功", response.getMsg());
    }

    @Test
    void productionLoadShouldReturnApiResponsePage() {
        ApsResourceLoadClient client = mock(ApsResourceLoadClient.class);
        when(client.list(null, null, null, null, null)).thenReturn(Result.success(List.of(
                Map.of(
                        "id", 1L,
                        "departmentName", "生产部",
                        "workCenterName", "产线A",
                        "timePeriod", "day"
                )
        )));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "apsResourceLoadClient", client);

        ApiResponse<PageResult<ProductionLoadDto>> response = controller.load(1, 10, null, null, null, null);

        assertEquals(200, response.getCode());
        assertEquals("生产负荷查询成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionEquipmentShouldReturnApiResponsePage() {
        EamAssetClient client = mock(EamAssetClient.class);
        when(client.list()).thenReturn(Result.success(List.of(
                Map.of(
                        "id", 7L,
                        "code", "EQ-1",
                        "name", "设备A",
                        "status", "online"
                )
        )));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "eamAssetClient", client);

        ApiResponse<PageResult<EquipmentDto>> response = controller.equipment(1, 10);

        assertEquals(200, response.getCode());
        assertEquals("设备列表查询成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionEquipmentStatusShouldReturnApiResponseSummary() {
        EamAssetClient client = mock(EamAssetClient.class);
        when(client.list()).thenReturn(Result.success(List.of(
                Map.of("id", 1L, "status", "online"),
                Map.of("id", 2L, "status", "offline")
        )));

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "eamAssetClient", client);

        ApiResponse<Map<String, Object>> response = controller.equipmentStatus();

        assertEquals(200, response.getCode());
        assertEquals("设备状态查询成功", response.getMsg());
        assertEquals(2, response.getData().get("total"));
    }

    @Test
    void productionCapacityShouldReturnApiResponsePage() {
        ProductionService service = mock(ProductionService.class);
        PageResult<CapacityDataDto> page = PageResult.build(1L, 10, 1, List.of(new CapacityDataDto()));
        when(service.getCapacity(1, 10, null, null, null, null, null, null)).thenReturn(page);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<PageResult<CapacityDataDto>> response = controller.getCapacity(
                1, 10,
                null, null,
                null, null,
                null, null,
                null,
                null, null,
                null, null
        );

        assertEquals(200, response.getCode());
        assertEquals("获取产能数据成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionRunCapacityPlanningShouldReturnApiResponse() {
        ApsSchedulingClient schedulingClient = mock(ApsSchedulingClient.class);
        ProductionService service = mock(ProductionService.class);
        PageResult<CapacityPlanningResultDto> page = PageResult.build(1L, 10, 1, List.of(new CapacityPlanningResultDto()));
        when(service.runCapacityPlanning(Map.of("planId", 1L))).thenReturn(page);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "apsSchedulingClient", schedulingClient);
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<PageResult<CapacityPlanningResultDto>> response = controller.runCapacityPlanning(Map.of("planId", 1L));

        assertEquals(200, response.getCode());
        assertEquals("产能规划执行成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }

    @Test
    void productionCapacityPlanningResultsShouldReturnApiResponse() {
        ProductionService service = mock(ProductionService.class);
        PageResult<CapacityPlanningResultDto> page = PageResult.build(1L, 10, 1, List.of(new CapacityPlanningResultDto()));
        when(service.getCapacityPlanningResults(1, 10)).thenReturn(page);

        ProductionController controller = new ProductionController();
        ReflectionTestUtils.setField(controller, "productionService", service);

        ApiResponse<PageResult<CapacityPlanningResultDto>> response = controller.capacityPlanningResults(1, 10);

        assertEquals(200, response.getCode());
        assertEquals("产能规划结果查询成功", response.getMsg());
        assertEquals(1L, response.getData().getTotal());
    }
}
