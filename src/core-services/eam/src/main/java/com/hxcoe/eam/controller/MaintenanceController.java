package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.entity.FaultRecordEntity;
import com.hxcoe.eam.entity.MaintenancePlanEntity;
import com.hxcoe.eam.entity.MaintenanceRecordEntity;
import com.hxcoe.eam.entity.WorkOrderEntity;
import com.hxcoe.eam.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eam/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping("/plans")
    public Result<List<MaintenancePlanEntity>> getAllPlans() {
        return Result.success(maintenanceService.getAllPlans());
    }

    @PostMapping("/plans")
    public Result<MaintenancePlanEntity> createPlan(@RequestBody MaintenancePlanEntity plan) {
        return Result.success(maintenanceService.savePlan(plan));
    }
    
    @PutMapping("/plans/{id}")
    public Result<MaintenancePlanEntity> updatePlan(@PathVariable Long id, @RequestBody MaintenancePlanEntity plan) {
        plan.setId(id);
        return Result.success(maintenanceService.savePlan(plan));
    }

    @GetMapping("/workorders")
    public Result<List<WorkOrderEntity>> getAllWorkOrders() {
        return Result.success(maintenanceService.getAllWorkOrders());
    }
    
    @PostMapping("/workorders")
    public Result<WorkOrderEntity> createWorkOrder(@RequestBody WorkOrderEntity workOrder) {
        return Result.success(maintenanceService.saveWorkOrder(workOrder));
    }
    
    @PutMapping("/workorders/{id}")
    public Result<WorkOrderEntity> updateWorkOrder(@PathVariable Long id, @RequestBody WorkOrderEntity workOrder) {
        workOrder.setId(id);
        return Result.success(maintenanceService.saveWorkOrder(workOrder));
    }
    
    @GetMapping("/faults")
    public Result<List<FaultRecordEntity>> getAllFaults() {
        return Result.success(maintenanceService.getAllFaultRecords());
    }
    
    @PostMapping("/faults")
    public Result<FaultRecordEntity> createFault(@RequestBody FaultRecordEntity fault) {
        return Result.success(maintenanceService.saveFaultRecord(fault));
    }
    
    @PutMapping("/faults/{id}")
    public Result<FaultRecordEntity> updateFault(@PathVariable Long id, @RequestBody FaultRecordEntity fault) {
        fault.setId(id);
        return Result.success(maintenanceService.saveFaultRecord(fault));
    }
    
    @GetMapping("/records")
    public Result<List<MaintenanceRecordEntity>> getAllRecords() {
        return Result.success(maintenanceService.getAllMaintenanceRecords());
    }
    
    @PostMapping("/records")
    public Result<MaintenanceRecordEntity> createRecord(@RequestBody MaintenanceRecordEntity record) {
        return Result.success(maintenanceService.saveMaintenanceRecord(record));
    }

    @PostMapping("/plans/{id}/execute")
    public Result<WorkOrderEntity> executePlan(@PathVariable Long id) {
        return Result.success(maintenanceService.executePlan(id));
    }

    @DeleteMapping("/plans/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        maintenanceService.deletePlan(id);
        return Result.success();
    }

    @DeleteMapping("/workorders/{id}")
    public Result<Void> deleteWorkOrder(@PathVariable Long id) {
        maintenanceService.deleteWorkOrder(id);
        return Result.success();
    }

    @DeleteMapping("/faults/{id}")
    public Result<Void> deleteFault(@PathVariable Long id) {
        maintenanceService.deleteFaultRecord(id);
        return Result.success();
    }
}
