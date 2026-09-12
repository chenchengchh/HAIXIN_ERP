package com.hxcoe.opportunity.controller;

import com.hxcoe.opportunity.entity.OpportunityEntity;
import com.hxcoe.opportunity.service.OpportunityService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/opportunity/opportunities", "/opportunity/opportunities"})
public class OpportunityController {
    
    @Autowired
    private OpportunityService opportunityService;
    
    @PostMapping
    public ResponseEntity<Result<OpportunityEntity>> createOpportunity(@RequestBody OpportunityEntity opportunity) {
        OpportunityEntity createdOpportunity = opportunityService.createOpportunity(opportunity);
        return ResponseEntity.ok(Result.success(createdOpportunity));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Result<OpportunityEntity>> updateOpportunity(@PathVariable Long id, @RequestBody OpportunityEntity opportunity) {
        OpportunityEntity updatedOpportunity = opportunityService.updateOpportunity(id, opportunity);
        return ResponseEntity.ok(Result.success(updatedOpportunity));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(Result.success());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<OpportunityEntity>> getOpportunityById(@PathVariable Long id) {
        Optional<OpportunityEntity> opportunity = opportunityService.getOpportunityById(id);
        return opportunity.map(o -> ResponseEntity.ok(Result.success(o)))
                .orElseGet(() -> ResponseEntity.ok(Result.error("商机不存在")));
    }
    
    @GetMapping("/by-code/{opportunityCode}")
    public ResponseEntity<Result<OpportunityEntity>> getOpportunityByOpportunityCode(@PathVariable String opportunityCode) {
        OpportunityEntity opportunity = opportunityService.getOpportunityByOpportunityCode(opportunityCode);
        return ResponseEntity.ok(Result.success(opportunity));
    }
    
    @GetMapping
    public ResponseEntity<Result<List<OpportunityEntity>>> getAllOpportunities() {
        List<OpportunityEntity> opportunities = opportunityService.getAllOpportunities();
        return ResponseEntity.ok(Result.success(opportunities));
    }
    
    @GetMapping("/page")
    public ResponseEntity<Result<PageResult<OpportunityEntity>>> getOpportunitiesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        PageResult<OpportunityEntity> opportunities = opportunityService.getOpportunitiesByPage(pageable);
        return ResponseEntity.ok(Result.success(opportunities));
    }
    
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<Result<List<OpportunityEntity>>> getOpportunitiesByCustomerId(@PathVariable Long customerId) {
        List<OpportunityEntity> opportunities = opportunityService.getOpportunitiesByCustomerId(customerId);
        return ResponseEntity.ok(Result.success(opportunities));
    }
    
    @GetMapping("/by-owner/{owner}")
    public ResponseEntity<Result<List<OpportunityEntity>>> getOpportunitiesByOwner(@PathVariable String owner) {
        List<OpportunityEntity> opportunities = opportunityService.getOpportunitiesByOwner(owner);
        return ResponseEntity.ok(Result.success(opportunities));
    }
    
    @GetMapping("/by-stage/{stage}")
    public ResponseEntity<Result<List<OpportunityEntity>>> getOpportunitiesByStage(@PathVariable String stage) {
        List<OpportunityEntity> opportunities = opportunityService.getOpportunitiesByStage(stage);
        return ResponseEntity.ok(Result.success(opportunities));
    }
}