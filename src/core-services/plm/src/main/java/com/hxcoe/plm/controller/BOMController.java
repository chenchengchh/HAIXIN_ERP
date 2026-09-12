package com.hxcoe.plm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.BOMEntity;
import com.hxcoe.plm.service.BOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping({"/plm/boms", "/api/v1/plm/boms"})
public class BOMController {

    @Autowired
    private BOMService bomService;

    @PostMapping
    public ApiResponse<BOMEntity> createBOM(@RequestBody BOMEntity bom) {
        return ResultAdapter.fromResult(bomService.createBOM(bom));
    }

    @PutMapping("/{id}")
    public ApiResponse<BOMEntity> updateBOM(@PathVariable("id") Long id, @RequestBody BOMEntity bom) {
        return ResultAdapter.fromResult(bomService.updateBOM(id, bom));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBOM(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomService.deleteBOM(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<BOMEntity> getBOMById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomService.getBOMById(id));
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<BOMEntity>> getBOMsByProductId(
            @PathVariable("productId") Long productId,
            @RequestParam(name = "version", required = false) String version) {
        return ResultAdapter.fromResult(bomService.getBOMsByProductId(productId, version));
    }

    @PostMapping("/product/{productId}/checkout")
    public ApiResponse<Void> checkout(@PathVariable("productId") Long productId) {
        return ResultAdapter.fromResult(bomService.checkoutByProductId(productId));
    }

    @PostMapping("/product/{productId}/checkin")
    public ApiResponse<Void> checkin(@PathVariable("productId") Long productId) {
        return ResultAdapter.fromResult(bomService.checkinByProductId(productId));
    }

    @PostMapping("/product/{productId}/release")
    public ApiResponse<Void> release(@PathVariable("productId") Long productId) {
        return ResultAdapter.fromResult(bomService.releaseByProductId(productId));
    }

    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compare(
            @RequestParam(name = "productId") Long productId,
            @RequestParam(name = "fromVersion") String fromVersion,
            @RequestParam(name = "toVersion") String toVersion) {
        return ResultAdapter.fromResult(bomService.compareVersions(productId, fromVersion, toVersion));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<BOMEntity>> getBOMsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        int safePage = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(safePage, size);
        return ResultAdapter.fromResult(bomService.getBOMsByPage(pageable));
    }
}
