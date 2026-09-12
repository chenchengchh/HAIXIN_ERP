package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.DocumentEntity;
import com.hxcoe.oa.service.DocumentService;
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
 * 文档管理Controller
 */
@RestController
@RequestMapping("/api/v1/oa/document")
@Tag(name = "文档管理", description = "文档相关API")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    /**
     * 创建文档
     */
    @PostMapping
    @Operation(summary = "创建文档", description = "创建新的文档")
    public Result<DocumentEntity> createDocument(@RequestBody DocumentEntity document) {
        logger.debug("创建文档: {}", document);
        DocumentEntity createdDocument = documentService.createDocument(document);
        return Result.success(createdDocument);
    }

    /**
     * 更新文档
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文档", description = "更新指定ID的文档")
    public Result<DocumentEntity> updateDocument(@PathVariable Long id, @RequestBody DocumentEntity document) {
        logger.debug("更新文档: id={}, {}", id, document);
        DocumentEntity updatedDocument = documentService.updateDocument(id, document);
        return Result.success(updatedDocument);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档", description = "删除指定ID的文档")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        logger.debug("删除文档: id={}", id);
        documentService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 根据ID获取文档
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文档", description = "根据ID获取文档详情")
    public Result<DocumentEntity> getDocumentById(@PathVariable Long id) {
        logger.debug("获取文档: id={}", id);
        DocumentEntity document = documentService.getDocumentById(id);
        return Result.success(document);
    }

    /**
     * 获取文档列表
     */
    @GetMapping
    @Operation(summary = "获取文档列表", description = "获取所有文档列表")
    public Result<List<DocumentEntity>> getDocumentList() {
        logger.debug("获取文档列表");
        List<DocumentEntity> documents = documentService.getDocumentList();
        return Result.success(documents);
    }

    /**
     * 分页获取文档列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取文档列表", description = "分页获取文档列表")
    public Result<PageResult<DocumentEntity>> getDocumentPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("分页获取文档列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
        Page<DocumentEntity> documentPage = documentService.getDocumentPage(pageable);
        
        PageResult<DocumentEntity> pageResult = PageResult.build(
                documentPage.getTotalElements(),
                size,
                page,
                documentPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据分类ID获取文档列表
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类ID获取文档列表", description = "根据分类ID获取文档列表")
    public Result<List<DocumentEntity>> getDocumentByCategoryId(@PathVariable Long categoryId) {
        logger.debug("根据分类ID获取文档列表: categoryId={}", categoryId);
        List<DocumentEntity> documents = documentService.getDocumentByCategoryId(categoryId);
        return Result.success(documents);
    }

    /**
     * 根据状态获取文档列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取文档列表", description = "根据状态获取文档列表")
    public Result<List<DocumentEntity>> getDocumentByStatus(@PathVariable Integer status) {
        logger.debug("根据状态获取文档列表: status={}", status);
        List<DocumentEntity> documents = documentService.getDocumentByStatus(status);
        return Result.success(documents);
    }

    /**
     * 根据创建者ID获取文档列表
     */
    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "根据创建者ID获取文档列表", description = "根据创建者ID获取文档列表")
    public Result<List<DocumentEntity>> getDocumentByCreatorId(@PathVariable Long creatorId) {
        logger.debug("根据创建者ID获取文档列表: creatorId={}", creatorId);
        List<DocumentEntity> documents = documentService.getDocumentByCreatorId(creatorId);
        return Result.success(documents);
    }

    /**
     * 发布文档
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布文档", description = "发布指定ID的文档")
    public Result<DocumentEntity> publishDocument(@PathVariable Long id) {
        logger.debug("发布文档: id={}", id);
        DocumentEntity document = documentService.publishDocument(id);
        return Result.success(document);
    }

    /**
     * 撤销文档
     */
    @PutMapping("/{id}/revoke")
    @Operation(summary = "撤销文档", description = "撤销指定ID的文档")
    public Result<DocumentEntity> revokeDocument(@PathVariable Long id) {
        logger.debug("撤销文档: id={}", id);
        DocumentEntity document = documentService.revokeDocument(id);
        return Result.success(document);
    }

    /**
     * 归档文档
     */
    @PutMapping("/{id}/archive")
    @Operation(summary = "归档文档", description = "归档指定ID的文档")
    public Result<DocumentEntity> archiveDocument(@PathVariable Long id) {
        logger.debug("归档文档: id={}", id);
        DocumentEntity document = documentService.archiveDocument(id);
        return Result.success(document);
    }
}