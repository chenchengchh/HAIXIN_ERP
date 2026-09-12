package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.entity.SparePartEntity;
import com.hxcoe.eam.entity.SpareInventoryEntity;
import com.hxcoe.eam.entity.SpareDemandPlanEntity;
import com.hxcoe.eam.entity.SpareIssueEntity;
import com.hxcoe.eam.service.SparePartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eam/spares")
public class SparePartController {

    @Autowired
    private SparePartService sparePartService;

    @GetMapping
    public Result<List<SparePartEntity>> getAllSpareParts() {
        return Result.success(sparePartService.getAllSpareParts());
    }

    @PostMapping
    public Result<SparePartEntity> createSparePart(@RequestBody SparePartEntity sparePart) {
        return Result.success(sparePartService.saveSparePart(sparePart));
    }
    
    @PutMapping("/{id}")
    public Result<SparePartEntity> updateSparePart(@PathVariable Long id, @RequestBody SparePartEntity sparePart) {
        sparePart.setId(id);
        return Result.success(sparePartService.saveSparePart(sparePart));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteSparePart(@PathVariable Long id) {
        sparePartService.deleteSparePart(id);
        return Result.success();
    }
    
    @GetMapping("/inventory")
    public Result<List<SpareInventoryEntity>> getAllInventory() {
        return Result.success(sparePartService.getAllInventory());
    }
    
    @PostMapping("/inventory")
    public Result<SpareInventoryEntity> updateInventory(@RequestBody SpareInventoryEntity inventory) {
        return Result.success(sparePartService.saveInventory(inventory));
    }
    
    @GetMapping("/demand-plans")
    public Result<List<SpareDemandPlanEntity>> getAllDemandPlans() {
        return Result.success(sparePartService.getAllDemandPlans());
    }
    
    @GetMapping("/issues")
    public Result<List<SpareIssueEntity>> getAllIssues() {
        return Result.success(sparePartService.getAllIssues());
    }
    
    @PostMapping("/issues")
    public Result<SpareIssueEntity> createIssue(@RequestBody SpareIssueEntity issue) {
        return Result.success(sparePartService.saveIssue(issue));
    }
    
    @PutMapping("/issues/{id}/approve")
    public Result<SpareIssueEntity> approveIssue(@PathVariable Long id) {
        return Result.success(sparePartService.approveIssue(id));
    }

    @PutMapping("/issues/{id}/reject")
    public Result<SpareIssueEntity> rejectIssue(@PathVariable Long id) {
        return Result.success(sparePartService.rejectIssue(id));
    }

    @PutMapping("/issues/{id}/return")
    public Result<SpareIssueEntity> returnIssue(@PathVariable Long id) {
        return Result.success(sparePartService.returnIssue(id));
    }
}
