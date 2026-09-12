package com.hxcoe.plm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.ProductEntity;
import com.hxcoe.plm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/plm/products", "/api/v1/plm/products"})
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ApiResponse<ProductEntity> createProduct(@RequestBody ProductEntity product) {
        return ResultAdapter.fromResult(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductEntity> updateProduct(@PathVariable("id") Long id, @RequestBody ProductEntity product) {
        return ResultAdapter.fromResult(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(productService.deleteProduct(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductEntity> getProductById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(productService.getProductById(id));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<ProductEntity>> getProductsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type) {
        int safePage = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(safePage, size);
        return ResultAdapter.fromResult(productService.getProductsByPage(pageable, keyword, type));
    }
    
    @PostMapping("/{id}/release")
    public ApiResponse<Void> releaseProduct(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(productService.releaseProduct(id));
    }
}
