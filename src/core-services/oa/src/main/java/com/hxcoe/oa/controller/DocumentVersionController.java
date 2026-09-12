package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.DocumentVersionEntity;
import com.hxcoe.oa.service.DocumentVersionService;
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
 * 文档版本管理Controller
 */
@RestController
@RequestMapping("/api/v1/oa/document/versions")
@Tag(name = "文档版本管理", description = "文档版本相关API")
public class DocumentVersionController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentVersionController.class);

    @Autowired
    private DocumentVersionService documentVersionService;

    /**
     * 创建文档版本
     */
    @PostMapping
    @Operation(summary = "创建文档版本", description = "创建新的文档版本")
    public Result<DocumentVersionEntity> createDocumentVersion(@RequestBody DocumentVersionEntity version) {
        logger.debug("创建文档版本: {}", version);
        DocumentVersionEntity createdVersion = documentVersionService.createDocumentVersion(version);
        return Result.success(createdVersion);
    }

    /**
     * 更新文档版本
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文档版本", description = "更新指定ID的文档版本")
    public Result<DocumentVersionEntity> updateDocumentVersion(@PathVariable Long id, @RequestBody DocumentVersionEntity version) {
        logger.debug("更新文档版本: id={}, {}", id, version);
        DocumentVersionEntity updatedVersion = documentVersionService.updateDocumentVersion(id, version);
        return Result.success(updatedVersion);
    }

    /**
     * 删除文档版本
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档版本", description = "删除指定ID的文档版本")
    public Result<Void> deleteDocumentVersion(@PathVariable Long id) {
        logger.debug("删除文档版本: id={}", id);
        documentVersionService.deleteDocumentVersion(id);
        return Result.success();
    }

    /**
     * 根据ID获取文档版本
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文档版本", description = "根据ID获取文档版本详情")
    public Result<DocumentVersionEntity> getDocumentVersionById(@PathVariable Long id) {
        logger.debug("获取文档版本: id={}", id);
        DocumentVersionEntity version = documentVersionService.getDocumentVersionById(id);
        return Result.success(version);
    }

    /**
     * 获取文档版本列表
     */
    @GetMapping
    @Operation(summary = "获取文档版本列表", description = "获取所有文档版本列表")
    public Result<List<DocumentVersionEntity>> getDocumentVersionList() {
        logger.debug("获取文档版本列表");
        List<DocumentVersionEntity> versions = documentVersionService.getDocumentVersionList();
        return Result.success(versions);
    }

    /**
     * 分页获取文档版本列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取文档版本列表", description = "分页获取文档版本列表")
    public Result<PageResult<DocumentVersionEntity>> getDocumentVersionPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("分页获取文档版本列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
        Page<DocumentVersionEntity> versionPage = documentVersionService.getDocumentVersionPage(pageable);
        
        PageResult<DocumentVersionEntity> pageResult = PageResult.build(
                versionPage.getTotalElements(),
                size,
                page,
                versionPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据文档ID获取文档版本列表
     */
    @GetMapping("/document/{documentId}")
    @Operation(summary = "根据文档ID获取文档版本列表", description = "根据文档ID获取文档版本列表")
    public Result<List<DocumentVersionEntity>> getDocumentVersionByDocumentId(@PathVariable Long documentId) {
        logger.debug("根据文档ID获取文档版本列表: documentId={}", documentId);
        List<DocumentVersionEntity> versions = documentVersionService.getDocumentVersionByDocumentId(documentId);
        return Result.success(versions);
    }

    /**
     * 根据文档ID获取最新版本
     */
    @GetMapping("/document/{documentId}/latest")
    @Operation(summary = "根据文档ID获取最新版本", description = "根据文档ID获取最新版本")
    public Result<DocumentVersionEntity> getLatestVersionByDocumentId(@PathVariable Long documentId) {
        logger.debug("根据文档ID获取最新版本: documentId={}", documentId);
        DocumentVersionEntity version = documentVersionService.getLatestVersionByDocumentId(documentId);
        return Result.success(version);
    }

    /**
     * 根据文档ID和版本号获取文档版本
     */
    @GetMapping("/document/{documentId}/version/{version}")
    @Operation(summary = "根据文档ID和版本号获取文档版本", description = "根据文档ID和版本号获取文档版本")
    public Result<DocumentVersionEntity> getDocumentVersionByDocumentIdAndVersion(@PathVariable Long documentId, @PathVariable Integer version) {
        logger.debug("根据文档ID和版本号获取文档版本: documentId={}, version={}", documentId, version);
        DocumentVersionEntity documentVersion = documentVersionService.getDocumentVersionByDocumentIdAndVersion(documentId, version);
        return Result.success(documentVersion);
    }

    /**
     * 回滚文档到指定版本
     */
    @PutMapping("/{id}/rollback")
    @Operation(summary = "回滚文档到指定版本", description = "回滚文档到指定版本")
    public Result<DocumentVersionEntity> rollbackDocumentVersion(@PathVariable Long id) {
        logger.debug("回滚文档到指定版本: id={}", id);
        DocumentVersionEntity version = documentVersionService.rollbackDocumentVersion(id);
        return Result.success(version);
    }
}