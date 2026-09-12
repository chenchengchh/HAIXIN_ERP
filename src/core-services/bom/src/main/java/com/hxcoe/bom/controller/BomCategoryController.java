package com.hxcoe.bom.controller;

import com.hxcoe.bom.entity.BomCategoryEntity;
import com.hxcoe.bom.service.BomCategoryService;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/bom/category", "/api/v1/bom/category"})
public class BomCategoryController {

    @Autowired
    private BomCategoryService bomCategoryService;

    @GetMapping("/tree")
    public ApiResponse<List<BomCategoryEntity>> getCategoryTree() {
        return ResultAdapter.fromResult(bomCategoryService.getCategoryTree());
    }

    @GetMapping("/{id}")
    public ApiResponse<BomCategoryEntity> getCategoryDetail(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomCategoryService.getCategoryById(id));
    }

    @PostMapping
    public ApiResponse<BomCategoryEntity> createCategory(@RequestBody BomCategoryEntity category) {
        return ResultAdapter.fromResult(bomCategoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    public ApiResponse<BomCategoryEntity> updateCategory(@PathVariable("id") Long id, @RequestBody BomCategoryEntity category) {
        return ResultAdapter.fromResult(bomCategoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomCategoryService.deleteCategory(id));
    }
}

