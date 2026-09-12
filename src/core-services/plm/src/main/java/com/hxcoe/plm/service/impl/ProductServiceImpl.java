package com.hxcoe.plm.service.impl;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.entity.ProductEntity;
import com.hxcoe.plm.repository.ProductRepository;
import com.hxcoe.plm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.hxcoe.plm.client.BomClient;
import com.hxcoe.plm.client.ErpClient;
import com.hxcoe.plm.client.dto.bom.BomHeaderDTO;
import com.hxcoe.plm.client.dto.bom.BomLineDTO;
import com.hxcoe.plm.client.dto.bom.MaterialCreateDTO;
import com.hxcoe.plm.client.dto.bom.MaterialDTO;
import com.hxcoe.plm.client.dto.bom.MaterialEventRequest;
import com.hxcoe.plm.client.dto.erp.ErpMaterialDTO;
import com.hxcoe.plm.entity.BOMEntity;
import com.hxcoe.plm.repository.BOMRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BOMRepository bomRepository;

    @Autowired
    private BomClient bomClient;

    @Autowired
    private ErpClient erpClient;

    // ... (其他方法保持不变)

    @Transactional
    @Override
    public Result<Void> releaseProduct(Long id) {
        ProductEntity product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return Result.error("产品不存在");
        }

        try {
            String productCode = product.getProductCode();
            String productName = product.getProductName();
            String version = product.getVersion() == null || product.getVersion().isBlank() ? "V1.0" : product.getVersion();

            // 确保BOM镜像存在产品物料（ERP权威写入 + BOM镜像即时刷新）
            MaterialDTO material = ensureMaterialInBom(
                    productCode,
                    productName == null || productName.isBlank() ? productCode : productName,
                    product.getProductSpec(),
                    product.getProductType(),
                    product.getUnit());

            if (material == null || material.getId() == null) {
                logger.warn("产品发布时无法同步/查询BOM物料: {}", productCode);
            } else {
                BomHeaderDTO bomHeader = new BomHeaderDTO();
                bomHeader.setMaterialId(material.getId());
                bomHeader.setMaterialCode(productCode);
                bomHeader.setMaterialName(productName);
                bomHeader.setBomCode("EBOM-" + productCode + "-" + version);
                bomHeader.setVersion(version);
                bomHeader.setType(1);
                bomHeader.setStatus(1);
                bomHeader.setIsDefault(true);

                ApiResponse<BomHeaderDTO> createdHeaderRes = bomClient.createBom(bomHeader);
                Long headerId = createdHeaderRes != null && createdHeaderRes.getCode() != null && createdHeaderRes.getCode() == 200
                        && createdHeaderRes.getData() != null ? createdHeaderRes.getData().getId() : null;

                if (headerId == null) {
                    logger.warn("发布到BOM服务失败：创建BOM头失败，bomCode={}", bomHeader.getBomCode());
                } else {
                    List<BOMEntity> plmBoms = bomRepository.findByProductId(id);
                    int seq = 1;
                    for (BOMEntity plmBom : plmBoms) {
                        if (plmBom == null || plmBom.getMaterialCode() == null || plmBom.getMaterialCode().isBlank()) continue;

                        // 确保BOM镜像存在子件物料
                        MaterialDTO child = ensureMaterialInBom(
                                plmBom.getMaterialCode(),
                                plmBom.getMaterialName(),
                                plmBom.getMaterialSpec(),
                                null,
                                plmBom.getUnit());
                        if (child == null || child.getId() == null) continue;

                        BomLineDTO line = new BomLineDTO();
                        line.setChildMaterialId(child.getId());
                        line.setChildMaterialCode(plmBom.getMaterialCode());
                        line.setChildMaterialName(plmBom.getMaterialName());
                        line.setQuantity(plmBom.getQuantity());
                        line.setUnit(plmBom.getUnit());
                        line.setLevel(plmBom.getLevel() == null ? 1 : plmBom.getLevel());
                        line.setSequence(plmBom.getSortOrder() == null ? seq : plmBom.getSortOrder());
                        line.setScrapRate(BigDecimal.ZERO);

                        ApiResponse<BomLineDTO> addRes = bomClient.addBomLine(headerId, line);
                        if (addRes == null || addRes.getCode() == null || addRes.getCode() != 200) {
                            logger.warn("BOM子项写入失败: headerId={}, childCode={}", headerId, plmBom.getMaterialCode());
                        }
                        seq++;
                    }
                    logger.info("设计BOM已发布到BOM服务: {}", bomHeader.getBomCode());
                }
            }
        } catch (Exception e) {
            logger.warn("发布到BOM服务失败，将继续发布产品状态", e);
        }

        product.setStatus("RELEASED");
        product.setUpdatedTime(LocalDateTime.now());
        productRepository.save(product);
        return Result.success();
    }

    /**
     * 确保指定物料在BOM镜像中存在。
     * 流程：查BOM镜像 -> 不存在则写入ERP物料主数据（权威源） -> 通过BOM集成端点发布物料事件即时刷新镜像 -> 重新查询镜像。
     *
     * @param materialCode 物料编码
     * @param materialName 物料名称
     * @param materialSpec 物料规格
     * @param materialType 物料类型
     * @param unit         计量单位
     * @return BOM镜像中的物料（含镜像ID），失败时返回null
     */
    private MaterialDTO ensureMaterialInBom(String materialCode, String materialName, String materialSpec,
                                            String materialType, String unit) {
        MaterialDTO existing = queryBomMaterial(materialCode);
        if (existing != null) {
            return existing;
        }

        // 写入ERP物料主数据（编码已存在时视为成功，ERP为权威源）
        try {
            ErpMaterialDTO erpMaterial = new ErpMaterialDTO();
            erpMaterial.setMaterialCode(materialCode);
            erpMaterial.setMaterialName(materialName == null || materialName.isBlank() ? materialCode : materialName);
            erpMaterial.setMaterialType(materialType);
            erpMaterial.setSpecification(materialSpec);
            erpMaterial.setUnit(unit);
            erpMaterial.setStatus(1);
            erpMaterial.setApprovalStatus("approved");
            erpMaterial.setRemark("PLM产品发布同步");
            ApiResponse<ErpMaterialDTO> erpRes = erpClient.createMaterial(erpMaterial);
            if (erpRes == null || erpRes.getCode() == null
                    || (erpRes.getCode() != 200 && erpRes.getCode() != 0)) {
                String msg = erpRes == null ? null : erpRes.getMsg();
                // 物料编码已存在不属于失败，后续直接刷新镜像
                if (msg == null || !msg.contains("已存在")) {
                    logger.warn("ERP物料创建失败: code={}, msg={}", materialCode, msg);
                }
            }
        } catch (Exception e) {
            logger.warn("ERP物料创建异常: code={}, error={}", materialCode, e.getMessage());
        }

        // 通过BOM集成端点即时刷新物料镜像（幂等，事件键含时间戳避免与ERP事件冲突）
        try {
            String eventKey = "MATERIAL_CREATED:" + materialCode + ":PLM:" + LocalDateTime.now();
            MaterialEventRequest req = new MaterialEventRequest();
            req.setEventId(UUID.randomUUID().toString());
            req.setTraceId(req.getEventId());
            req.setEventType("MATERIAL_CREATED");
            req.setEventKey(eventKey);
            req.setIdempotencyKey(eventKey);
            req.setPartitionKey(materialCode);
            req.setEventVersion(1);
            req.setProducer("plm-service");
            req.setEventTime(LocalDateTime.now());

            MaterialEventRequest.MaterialPayload payload = new MaterialEventRequest.MaterialPayload();
            payload.setMaterialCode(materialCode);
            payload.setMaterialName(materialName == null || materialName.isBlank() ? materialCode : materialName);
            payload.setMaterialType(materialType);
            payload.setSpecification(materialSpec);
            payload.setUnit(unit);
            payload.setStatus(1);
            payload.setIsDeleted(0);
            payload.setUpdatedTime(LocalDateTime.now());
            req.setMaterial(payload);

            ApiResponse<Map<String, Object>> eventRes = bomClient.publishMaterialEvent(req);
            if (eventRes == null || eventRes.getCode() == null
                    || (eventRes.getCode() != 200 && eventRes.getCode() != 0)) {
                logger.warn("BOM物料镜像事件发布失败: code={}", materialCode);
            }
        } catch (Exception e) {
            logger.warn("BOM物料镜像事件发布异常: code={}, error={}", materialCode, e.getMessage());
        }

        return queryBomMaterial(materialCode);
    }

    /**
     * 按编码查询BOM物料镜像，未命中或异常时返回null。
     */
    private MaterialDTO queryBomMaterial(String materialCode) {
        try {
            ApiResponse<PageResult<MaterialDTO>> res = bomClient.getMaterials(materialCode, 1, 1);
            if (res != null && res.getCode() != null && (res.getCode() == 200 || res.getCode() == 0)
                    && res.getData() != null && res.getData().getRecords() != null
                    && !res.getData().getRecords().isEmpty()) {
                return res.getData().getRecords().get(0);
            }
        } catch (Exception e) {
            logger.warn("BOM物料查询异常: code={}, error={}", materialCode, e.getMessage());
        }
        return null;
    }
    @Override
    public Result<ProductEntity> createProduct(ProductEntity product) {
        if (product.getProductCode() == null || product.getProductCode().isEmpty()) {
            return Result.error("产品编码不能为空");
        }
        product.setCreatedTime(LocalDateTime.now());
        product.setUpdatedTime(LocalDateTime.now());
        ProductEntity savedProduct = productRepository.save(product);
        return Result.success(savedProduct);
    }

    @Override
    public Result<ProductEntity> updateProduct(Long id, ProductEntity product) {
        ProductEntity existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            return Result.error("产品不存在");
        }
        // 确保不覆盖关键字段
        product.setId(id);
        product.setCreatedTime(existingProduct.getCreatedTime());
        product.setUpdatedTime(LocalDateTime.now());
        ProductEntity updatedProduct = productRepository.save(product);
        return Result.success(updatedProduct);
    }

    @Override
    public Result<Void> deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return Result.error("产品不存在");
        }
        productRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<ProductEntity> getProductById(Long id) {
        ProductEntity product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }

    public Result<PageResult<ProductEntity>> getProductsByPage(Pageable pageable) {
        return getProductsByPage(pageable, null, null);
    }

    @Override
    public Result<PageResult<ProductEntity>> getProductsByPage(Pageable pageable, String keyword, String type) {
        Specification<ProductEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("productName"), like),
                        cb.like(root.get("productCode"), like)
                ));
            }
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("productType"), type.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductEntity> page = productRepository.findAll(spec, pageable);
        PageResult<ProductEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            page.getSize(), 
            page.getNumber() + 1, 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
