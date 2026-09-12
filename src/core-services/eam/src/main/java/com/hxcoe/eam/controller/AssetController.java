package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.client.MesEquipmentStatusClient;
import com.hxcoe.eam.entity.AssetEntity;
import com.hxcoe.eam.entity.AssetCategoryEntity;
import com.hxcoe.eam.entity.AssetDocumentEntity;
import com.hxcoe.eam.entity.AssetHierarchyEntity;
import com.hxcoe.eam.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/eam/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired(required = false)
    private MesEquipmentStatusClient mesEquipmentStatusClient;

    @GetMapping
    public Result<List<AssetEntity>> getAllAssets() {
        return Result.success(assetService.getAllAssets());
    }

    @GetMapping("/{id}")
    public Result<AssetEntity> getAssetById(@PathVariable Long id) {
        return Result.success(assetService.getAssetById(id).orElse(null));
    }

    @PostMapping
    public Result<AssetEntity> createAsset(@RequestBody AssetEntity asset) {
        return Result.success(assetService.saveAsset(asset));
    }

    @PutMapping("/{id}")
    public Result<AssetEntity> updateAsset(@PathVariable Long id, @RequestBody AssetEntity asset) {
        asset.setId(id);
        // 先取更新前状态，用于判断状态是否实际变化
        String oldStatus = assetService.getAssetById(id).map(AssetEntity::getStatus).orElse(null);
        AssetEntity saved = assetService.saveAsset(asset);
        // 状态实际变化后推送 MES（生产侧感知设备维修中等状态）
        if (saved != null && saved.getStatus() != null && !saved.getStatus().equals(oldStatus)) {
            pushEquipmentStatusToMes(saved);
        }
        return Result.success(saved);
    }

    /**
     * 推送设备资产状态变更事件到 MES（EAM→MES 闭环）。
     *
     * <p>推送失败仅记录 warn 日志，不影响本地资产保存结果。
     *
     * @param asset 已保存的资产实体
     */
    private void pushEquipmentStatusToMes(AssetEntity asset) {
        if (mesEquipmentStatusClient == null) {
            log.warn("MesEquipmentStatusClient 不可用，跳过推送 assetCode={}", asset.getCode());
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("assetCode", asset.getCode());
            body.put("assetName", asset.getName());
            body.put("status", asset.getStatus());
            body.put("eventTime", LocalDateTime.now().toString());
            mesEquipmentStatusClient.pushEquipmentStatus(body);
            log.info("EAM->MES 设备状态推送成功 assetCode={} status={}", asset.getCode(), asset.getStatus());
        } catch (Exception e) {
            log.warn("EAM->MES 设备状态推送失败 assetCode={} status={}，原因：{}",
                    asset.getCode(), asset.getStatus(), e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success();
    }
    
    @GetMapping("/categories")
    public Result<List<AssetCategoryEntity>> getAllCategories() {
        return Result.success(assetService.getAllCategories());
    }
    
    @PostMapping("/categories")
    public Result<AssetCategoryEntity> createCategory(@RequestBody AssetCategoryEntity category) {
        return Result.success(assetService.saveCategory(category));
    }

    @PutMapping("/categories/{id}")
    public Result<AssetCategoryEntity> updateCategory(@PathVariable Long id, @RequestBody AssetCategoryEntity category) {
        category.setId(id);
        return Result.success(assetService.saveCategory(category));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        assetService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 查询设备层次结构（父子设备组成关系）全量列表。
     *
     * @return 层次结构记录列表
     */
    @GetMapping("/hierarchy")
    public Result<List<AssetHierarchyEntity>> getAllHierarchies() {
        return Result.success(assetService.getAllHierarchies());
    }

    /**
     * 新增设备层次结构节点。
     *
     * @param hierarchy 层次结构实体（parentId/childId/componentType/quantity/remark）
     * @return 保存后的实体
     */
    @PostMapping("/hierarchy")
    public Result<AssetHierarchyEntity> createHierarchy(@RequestBody AssetHierarchyEntity hierarchy) {
        hierarchy.setId(null);
        return Result.success(assetService.saveHierarchy(hierarchy));
    }

    /**
     * 更新设备层次结构节点。
     *
     * @param id 节点ID
     * @param hierarchy 层次结构实体
     * @return 保存后的实体
     */
    @PutMapping("/hierarchy/{id}")
    public Result<AssetHierarchyEntity> updateHierarchy(@PathVariable Long id, @RequestBody AssetHierarchyEntity hierarchy) {
        hierarchy.setId(id);
        return Result.success(assetService.saveHierarchy(hierarchy));
    }

    /**
     * 删除设备层次结构节点。
     *
     * @param id 节点ID
     * @return 空结果
     */
    @DeleteMapping("/hierarchy/{id}")
    public Result<Void> deleteHierarchy(@PathVariable Long id) {
        assetService.deleteHierarchy(id);
        return Result.success();
    }

    /**
     * 查询设备文档列表（不含文件内容大字段）。
     *
     * @return 文档列表
     */
    @GetMapping("/documents")
    public Result<List<AssetDocumentEntity>> getAllDocuments() {
        return Result.success(assetService.getAllDocuments());
    }

    /**
     * 新增设备文档（content 为带 "base64:" 前缀的文件内容，可为空）。
     *
     * @param document 文档实体
     * @return 保存后的实体
     */
    @PostMapping("/documents")
    public Result<AssetDocumentEntity> createDocument(@RequestBody AssetDocumentEntity document) {
        document.setId(null);
        return Result.success(assetService.saveDocument(document));
    }

    /**
     * 更新设备文档元数据。
     *
     * @param id 文档ID
     * @param document 文档实体
     * @return 保存后的实体
     */
    @PutMapping("/documents/{id}")
    public Result<AssetDocumentEntity> updateDocument(@PathVariable Long id, @RequestBody AssetDocumentEntity document) {
        document.setId(id);
        // content 被 @JsonIgnore 反序列化时为 null：未传内容则保留原文件内容
        if (document.getContent() == null) {
            assetService.getDocumentById(id).ifPresent(existing -> document.setContent(existing.getContent()));
        }
        return Result.success(assetService.saveDocument(document));
    }

    /**
     * 删除设备文档。
     *
     * @param id 文档ID
     * @return 空结果
     */
    @DeleteMapping("/documents/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        assetService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 下载设备文档：返回文档元数据及 Base64 文件内容（content 字段）。
     *
     * @param id 文档ID
     * @return 含文件内容的文档实体
     */
    @GetMapping("/documents/{id}/download")
    public Result<Map<String, Object>> downloadDocument(@PathVariable Long id) {
        AssetDocumentEntity doc = assetService.getDocumentById(id).orElse(null);
        if (doc == null) {
            return Result.success(null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", doc.getId());
        data.put("name", doc.getName());
        data.put("fileName", doc.getFileName());
        data.put("type", doc.getType());
        data.put("content", doc.getContent());
        return Result.success(data);
    }
}
