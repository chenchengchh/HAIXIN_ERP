package com.hxcoe.opportunity.controller;

import com.hxcoe.opportunity.entity.OpportunityTrackEntity;
import com.hxcoe.opportunity.service.OpportunityTrackService;
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
@RequestMapping({"/api/v1/opportunity/opportunity-tracks", "/opportunity/opportunity-tracks"})
public class OpportunityTrackController {
    
    @Autowired
    private OpportunityTrackService opportunityTrackService;
    
    @PostMapping
    public ResponseEntity<Result<OpportunityTrackEntity>> createOpportunityTrack(@RequestBody OpportunityTrackEntity track) {
        OpportunityTrackEntity createdTrack = opportunityTrackService.createOpportunityTrack(track);
        return ResponseEntity.ok(Result.success(createdTrack));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Result<OpportunityTrackEntity>> updateOpportunityTrack(@PathVariable Long id, @RequestBody OpportunityTrackEntity track) {
        OpportunityTrackEntity updatedTrack = opportunityTrackService.updateOpportunityTrack(id, track);
        return ResponseEntity.ok(Result.success(updatedTrack));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteOpportunityTrack(@PathVariable Long id) {
        opportunityTrackService.deleteOpportunityTrack(id);
        return ResponseEntity.ok(Result.success());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<OpportunityTrackEntity>> getOpportunityTrackById(@PathVariable Long id) {
        Optional<OpportunityTrackEntity> track = opportunityTrackService.getOpportunityTrackById(id);
        return track.map(t -> ResponseEntity.ok(Result.success(t)))
                .orElseGet(() -> ResponseEntity.ok(Result.error("跟踪记录不存在")));
    }
    
    @GetMapping
    public ResponseEntity<Result<List<OpportunityTrackEntity>>> getAllOpportunityTracks() {
        List<OpportunityTrackEntity> tracks = opportunityTrackService.getAllOpportunityTracks();
        return ResponseEntity.ok(Result.success(tracks));
    }
    
    @GetMapping("/page")
    public ResponseEntity<Result<PageResult<OpportunityTrackEntity>>> getOpportunityTracksByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        PageResult<OpportunityTrackEntity> tracks = opportunityTrackService.getOpportunityTracksByPage(pageable);
        return ResponseEntity.ok(Result.success(tracks));
    }
    
    @GetMapping("/by-opportunity/{opportunityId}")
    public ResponseEntity<Result<List<OpportunityTrackEntity>>> getOpportunityTracksByOpportunityId(@PathVariable Long opportunityId) {
        List<OpportunityTrackEntity> tracks = opportunityTrackService.getOpportunityTracksByOpportunityId(opportunityId);
        return ResponseEntity.ok(Result.success(tracks));
    }
    
    @GetMapping("/by-tracker/{tracker}")
    public ResponseEntity<Result<List<OpportunityTrackEntity>>> getOpportunityTracksByTracker(@PathVariable String tracker) {
        List<OpportunityTrackEntity> tracks = opportunityTrackService.getOpportunityTracksByTracker(tracker);
        return ResponseEntity.ok(Result.success(tracks));
    }
}