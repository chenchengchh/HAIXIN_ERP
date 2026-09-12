package com.hxcoe.eam.service;

import com.hxcoe.eam.entity.MaintenancePlanEntity;
import com.hxcoe.eam.entity.WorkOrderEntity;
import com.hxcoe.eam.entity.FaultRecordEntity;
import com.hxcoe.eam.entity.MaintenanceRecordEntity;
import com.hxcoe.eam.repository.MaintenancePlanRepository;
import com.hxcoe.eam.repository.WorkOrderRepository;
import com.hxcoe.eam.repository.FaultRecordRepository;
import com.hxcoe.eam.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenancePlanRepository planRepository;
    
    @Autowired
    private WorkOrderRepository workOrderRepository;
    
    @Autowired
    private FaultRecordRepository faultRecordRepository;
    
    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    public List<MaintenancePlanEntity> getAllPlans() {
        return planRepository.findAll();
    }

    public MaintenancePlanEntity savePlan(MaintenancePlanEntity plan) {
        return planRepository.save(plan);
    }

    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    public List<WorkOrderEntity> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }
    
    public WorkOrderEntity saveWorkOrder(WorkOrderEntity workOrder) {
        return workOrderRepository.save(workOrder);
    }
    
    public void deleteWorkOrder(Long id) {
        workOrderRepository.deleteById(id);
    }
    
    public List<FaultRecordEntity> getAllFaultRecords() {
        return faultRecordRepository.findAll();
    }
    
    public FaultRecordEntity saveFaultRecord(FaultRecordEntity record) {
        return faultRecordRepository.save(record);
    }

    public void deleteFaultRecord(Long id) {
        faultRecordRepository.deleteById(id);
    }
    
    public List<MaintenanceRecordEntity> getAllMaintenanceRecords() {
        return maintenanceRecordRepository.findAll();
    }
    
    public MaintenanceRecordEntity saveMaintenanceRecord(MaintenanceRecordEntity record) {
        return maintenanceRecordRepository.save(record);
    }
    
    // 业务逻辑：执行计划生成工单
    public WorkOrderEntity executePlan(Long planId) {
        MaintenancePlanEntity plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
            
        WorkOrderEntity workOrder = new WorkOrderEntity();
        workOrder.setEquipmentId(plan.getEquipmentId());
        workOrder.setEquipmentName(plan.getEquipmentName());
        workOrder.setType(plan.getType());
        workOrder.setDescription("Generated from plan: " + plan.getDescription());
        workOrder.setPriority("medium");
        workOrder.setStatus("pending");
        workOrder.setPlanId(planId);
        
        return workOrderRepository.save(workOrder);
    }
}
