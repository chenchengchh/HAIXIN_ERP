package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import com.hxcoe.erp.dto.integration.LocationEventRequest;
import com.hxcoe.erp.dto.integration.SupplierEventRequest;
import com.hxcoe.erp.dto.integration.WarehouseEventRequest;
import com.hxcoe.erp.dto.integration.PoFactRequest;
import com.hxcoe.erp.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.erp.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.erp.service.SupplierMirrorService;
import com.hxcoe.erp.service.WarehouseLocationMirrorService;
import com.hxcoe.erp.service.FinanceTaskService;
import com.hxcoe.erp.service.ErpPoSnapshotService;
import com.hxcoe.erp.service.ErpOutboundShipmentFactService;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/erp/integration", "/erp/integration"})
public class IntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);

    @Autowired
    private WarehouseLocationMirrorService warehouseLocationMirrorService;

    @Autowired
    private SupplierMirrorService supplierMirrorService;

    @Autowired
    private FinanceTaskService financeTaskService;

    @Autowired
    private ErpPoSnapshotService erpPoSnapshotService;

    @Autowired
    private ErpOutboundShipmentFactService erpOutboundShipmentFactService;

    @PostMapping("/wms/warehouse-events")
    public ApiResponse<Map<String, Object>> receiveWarehouseEvents(@RequestBody WarehouseEventRequest req) {
        warehouseLocationMirrorService.applyWarehouseEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/wms/location-events")
    public ApiResponse<Map<String, Object>> receiveLocationEvents(@RequestBody LocationEventRequest req) {
        warehouseLocationMirrorService.applyLocationEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/srm/supplier-events")
    public ApiResponse<Map<String, Object>> receiveSupplierEvents(@RequestBody SupplierEventRequest req) {
        supplierMirrorService.applySrmEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/scm/po-facts")
    public ApiResponse<Map<String, Object>> receiveScmPoFacts(@RequestBody PoFactRequest req) {
        if (req == null || req.getFact() == null || req.getFact().getPoNo() == null || req.getFact().getPoNo().isBlank()) {
            return ApiResponse.error(400, "poNo不能为空");
        }
        if (req.getFact().getFactType() == null || req.getFact().getFactType().isBlank()) {
            return ApiResponse.error(400, "factType不能为空");
        }
        FinanceTaskEntity task = financeTaskService.createFromPoFact(req);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task == null ? null : task.getId());
        data.put("idempotencyKey", req.getIdempotencyKey());
        data.put("eventId", req.getEventId());
        data.put("traceId", req.getTraceId());
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/scm/purchase-order-events")
    public ApiResponse<Map<String, Object>> receiveScmPoEvents(@RequestBody PurchaseOrderEventRequest req) {
        boolean ok = erpPoSnapshotService.applyScmPoEvent(req);
        if (!ok) {
            return ApiResponse.error(409, "事件重复或处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        data.put("eventId", req == null ? null : req.getEventId());
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/wms/outbound-shipped")
    public ApiResponse<Map<String, Object>> receiveWmsOutboundShipped(@RequestBody WmsOutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null || req.getOutboundOrder().getOrderNo() == null || req.getOutboundOrder().getOrderNo().isBlank()) {
            return ApiResponse.error(400, "orderNo不能为空");
        }
        boolean ok = erpOutboundShipmentFactService.applyWmsOutboundShipped(req);
        if (!ok) {
            return ApiResponse.error(409, "事件重复或处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req.getEventKey());
        data.put("eventId", req.getEventId());
        data.put("orderNo", req.getOutboundOrder().getOrderNo());
        return ApiResponse.success("成功", data);
    }

    /**
     * 接收 LES 签收完成事件，回写 ERP 订单/出库状态。
     * <p>P2-A: LES 签收回流入口。本期记录事件落地，后续可扩展更新出库单签收状态。</p>
     *
     * @param req LES 签收完成事件
     * @return 处理结果
     */
    @PostMapping("/les/sign-completed")
    public ApiResponse<Map<String, Object>> receiveLesSignCompleted(@RequestBody LesSignCompletedEventDTO req) {
        if (req == null || req.getSignVoucher() == null) {
            return ApiResponse.error(400, "签收凭证载荷不能为空");
        }
        LesSignCompletedEventDTO.SignVoucherPayload payload = req.getSignVoucher();
        if (payload.getErpOrderNo() == null || payload.getErpOrderNo().isBlank()) {
            return ApiResponse.error(400, "erpOrderNo不能为空");
        }
        logger.info("LES签收回流 ERP eventId={} erpOrderNo={} voucherId={} status={}",
                req.getEventId(), payload.getErpOrderNo(), payload.getSignVoucherId(), payload.getStatus());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", req.getEventId());
        data.put("idempotencyKey", req.getIdempotencyKey());
        data.put("erpOrderNo", payload.getErpOrderNo());
        return ApiResponse.success("成功", data);
    }

    /**
     * 接收 HR 员工事件，同步员工主数据镜像。
     * <p>P2-C: HR→ERP 员工事件落地入口。本期记录事件日志，后续可扩展员工镜像表更新。</p>
     *
     * @param req HR 员工事件
     * @return 处理结果
     */
    @PostMapping("/hr/employee-event")
    public ApiResponse<Map<String, Object>> receiveHrEmployeeEvent(@RequestBody EmployeeEventDTO req) {
        if (req == null || req.getEmployee() == null) {
            return ApiResponse.error(400, "员工载荷不能为空");
        }
        EmployeeEventDTO.EmployeePayload payload = req.getEmployee();
        logger.info("HR员工事件 ERP eventId={} eventType={} employeeId={} employeeNo={} employeeName={} departmentId={}",
                req.getEventId(), req.getEventType(), payload.getEmployeeId(), payload.getEmployeeNo(),
                payload.getEmployeeName(), payload.getDepartmentId());
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", req.getEventId());
        data.put("idempotencyKey", req.getIdempotencyKey());
        data.put("eventType", req.getEventType());
        data.put("employeeNo", payload.getEmployeeNo());
        return ApiResponse.success("成功", data);
    }
}
