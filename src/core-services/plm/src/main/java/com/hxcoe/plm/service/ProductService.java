package com.hxcoe.plm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.ProductEntity;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Result<ProductEntity> createProduct(ProductEntity product);
    Result<ProductEntity> updateProduct(Long id, ProductEntity product);
    Result<Void> deleteProduct(Long id);
    Result<ProductEntity> getProductById(Long id);
    Result<PageResult<ProductEntity>> getProductsByPage(Pageable pageable, String keyword, String type);
    
    /**
     * 发布产品设计到BOM服务
     */
    Result<Void> releaseProduct(Long id);
}
