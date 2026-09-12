package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.dto.WorkOrderStatusUpdateRequest;
import com.hxcoe.mes.entity.WorkOrderEntity;
import com.hxcoe.mes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.hxcoe.mes.client.dto.qms.InspectionResultDTO;

@RestController
@RequestMapping({"/mes/work-orders", "/mes/v1/work-orders", "/api/v1/mes/work-orders", "/api/mes/work-orders"})
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    // ... (其他方法保持不变)

    @PostMapping("/inspection-result")
    public Result<Void> receiveInspectionResult(@RequestBody InspectionResultDTO resultDTO) {
        workOrderService.handleInspectionResult(resultDTO);
        return Result.success();
    }

    @PostMapping
    public Result<WorkOrderEntity> createWorkOrder(@RequestBody WorkOrderEntity workOrder) {
        return Result.success(workOrderService.createWorkOrder(workOrder));
    }

    @GetMapping
    public Result<List<WorkOrderEntity>> getWorkOrders() {
        return Result.success(workOrderService.getAllWorkOrders());
    }

    @GetMapping("/no/{workOrderNo}")
    public Result<WorkOrderEntity> getByNo(@PathVariable String workOrderNo) {
        Optional<WorkOrderEntity> order = workOrderService.getByWorkOrderNo(workOrderNo);
        return Result.success(order.orElse(null));
    }
    
    @PutMapping("/{id}/status")
    public Result<WorkOrderEntity> updateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) WorkOrderStatusUpdateRequest body,
            @RequestParam(required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        return Result.success(workOrderService.updateStatus(id, nextStatus));
    }

    @PutMapping("/no/{workOrderNo}/status")
    public Result<WorkOrderEntity> updateStatusByNo(
            @PathVariable String workOrderNo,
            @RequestBody(required = false) WorkOrderStatusUpdateRequest body,
            @RequestParam(required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        return Result.success(workOrderService.updateStatusByWorkOrderNo(workOrderNo, nextStatus));
    }
}
