package com.hxcoe.eam.service;

import com.hxcoe.eam.entity.AssetEntity;
import com.hxcoe.eam.entity.AssetCategoryEntity;
import com.hxcoe.eam.entity.AssetDocumentEntity;
import com.hxcoe.eam.entity.AssetHierarchyEntity;
import com.hxcoe.eam.repository.AssetRepository;
import com.hxcoe.eam.repository.AssetCategoryRepository;
import com.hxcoe.eam.repository.AssetDocumentRepository;
import com.hxcoe.eam.repository.AssetHierarchyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetHierarchyRepository hierarchyRepository;

    @Autowired
    private AssetDocumentRepository documentRepository;

    public List<AssetEntity> getAllAssets() {
        return assetRepository.findAll();
    }

    public Optional<AssetEntity> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    public AssetEntity saveAsset(AssetEntity asset) {
        return assetRepository.save(asset);
    }

    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }
    
    public List<AssetCategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public AssetCategoryEntity saveCategory(AssetCategoryEntity category) {
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /**
     * 查询全部设备层次结构记录。
     *
     * @return 层次结构列表（父子设备组成关系）
     */
    public List<AssetHierarchyEntity> getAllHierarchies() {
        return hierarchyRepository.findAll();
    }

    /**
     * 保存（新增或更新）设备层次结构节点。
     *
     * @param hierarchy 层次结构实体
     * @return 保存后的实体
     */
    public AssetHierarchyEntity saveHierarchy(AssetHierarchyEntity hierarchy) {
        return hierarchyRepository.save(hierarchy);
    }

    /**
     * 删除设备层次结构节点。
     *
     * @param id 节点ID
     */
    public void deleteHierarchy(Long id) {
        hierarchyRepository.deleteById(id);
    }

    /**
     * 查询全部设备文档（不含文件内容大字段，content 已 @JsonIgnore）。
     *
     * @return 文档列表
     */
    public List<AssetDocumentEntity> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * 按ID查询设备文档。
     *
     * @param id 文档ID
     * @return 文档实体（含 content，用于下载）
     */
    public Optional<AssetDocumentEntity> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * 保存（新增或更新）设备文档。
     *
     * @param document 文档实体
     * @return 保存后的实体
     */
    public AssetDocumentEntity saveDocument(AssetDocumentEntity document) {
        return documentRepository.save(document);
    }

    /**
     * 删除设备文档。
     *
     * @param id 文档ID
     */
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}
