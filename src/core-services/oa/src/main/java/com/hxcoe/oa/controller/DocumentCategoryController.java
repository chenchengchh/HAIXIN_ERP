package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.DocumentCategoryEntity;
import com.hxcoe.oa.service.DocumentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档分类管理Controller
 */
@RestController
@RequestMapping("/api/v1/oa/document/category")
@Tag(name = "文档分类管理", description = "文档分类相关API")
public class DocumentCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCategoryController.class);

    @Autowired
    private DocumentCategoryService documentCategoryService;

    /**
     * 创建文档分类
     */
    @PostMapping
    @Operation(summary = "创建文档分类", description = "创建新的文档分类")
    public Result<DocumentCategoryEntity> createDocumentCategory(@RequestBody DocumentCategoryEntity category) {
        logger.debug("创建文档分类: {}", category);
        DocumentCategoryEntity createdCategory = documentCategoryService.createDocumentCategory(category);
        return Result.success(createdCategory);
    }

    /**
     * 更新文档分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文档分类", description = "更新指定ID的文档分类")
    public Result<DocumentCategoryEntity> updateDocumentCategory(@PathVariable Long id, @RequestBody DocumentCategoryEntity category) {
        logger.debug("更新文档分类: id={}, {}", id, category);
        DocumentCategoryEntity updatedCategory = documentCategoryService.updateDocumentCategory(id, category);
        return Result.success(updatedCategory);
    }

    /**
     * 删除文档分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档分类", description = "删除指定ID的文档分类")
    public Result<Void> deleteDocumentCategory(@PathVariable Long id) {
        logger.debug("删除文档分类: id={}", id);
        documentCategoryService.deleteDocumentCategory(id);
        return Result.success();
    }

    /**
     * 根据ID获取文档分类
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文档分类", description = "根据ID获取文档分类详情")
    public Result<DocumentCategoryEntity> getDocumentCategoryById(@PathVariable Long id) {
        logger.debug("获取文档分类: id={}", id);
        DocumentCategoryEntity category = documentCategoryService.getDocumentCategoryById(id);
        return Result.success(category);
    }

    /**
     * 获取文档分类列表
     */
    @GetMapping
    @Operation(summary = "获取文档分类列表", description = "获取所有文档分类列表")
    public Result<List<DocumentCategoryEntity>> getDocumentCategoryList() {
        logger.debug("获取文档分类列表");
        List<DocumentCategoryEntity> categories = documentCategoryService.getDocumentCategoryList();
        return Result.success(categories);
    }

    /**
     * 分页获取文档分类列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取文档分类列表", description = "分页获取文档分类列表")
    public Result<PageResult<DocumentCategoryEntity>> getDocumentCategoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("分页获取文档分类列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
        Page<DocumentCategoryEntity> categoryPage = documentCategoryService.getDocumentCategoryPage(pageable);
        
        PageResult<DocumentCategoryEntity> pageResult = PageResult.build(
                categoryPage.getTotalElements(),
                size,
                page,
                categoryPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据父分类ID获取文档分类列表
     */
    @GetMapping("/parent/{parentId}")
    @Operation(summary = "根据父分类ID获取文档分类列表", description = "根据父分类ID获取文档分类列表")
    public Result<List<DocumentCategoryEntity>> getDocumentCategoryByParentId(@PathVariable Long parentId) {
        logger.debug("根据父分类ID获取文档分类列表: parentId={}", parentId);
        List<DocumentCategoryEntity> categories = documentCategoryService.getDocumentCategoryByParentId(parentId);
        return Result.success(categories);
    }

    /**
     * 根据状态获取文档分类列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取文档分类列表", description = "根据状态获取文档分类列表")
    public Result<List<DocumentCategoryEntity>> getDocumentCategoryByStatus(@PathVariable Integer status) {
        logger.debug("根据状态获取文档分类列表: status={}", status);
        List<DocumentCategoryEntity> categories = documentCategoryService.getDocumentCategoryByStatus(status);
        return Result.success(categories);
    }
}