package com.hxcoe.bom.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.bom.entity.BomHeaderEntity;
import com.hxcoe.bom.entity.BomLineEntity;
import com.hxcoe.bom.repository.BomHeaderRepository;
import com.hxcoe.bom.repository.BomLineRepository;
import com.hxcoe.bom.service.BomStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BOM结构管理Service实现类
 */
@Service
public class BomStructureServiceImpl implements BomStructureService {

    private static final Logger log = LoggerFactory.getLogger(BomStructureServiceImpl.class);

    @Autowired
    private BomHeaderRepository bomHeaderRepository;

    @Autowired
    private BomLineRepository bomLineRepository;

    @Override
    public Result<BomHeaderEntity> createBomHeader(BomHeaderEntity bomHeader) {
        // 设置默认值
        if (bomHeader.getIsDefault() == null) {
            bomHeader.setIsDefault(false);
        }
        if (bomHeader.getStatus() == null) {
            bomHeader.setStatus(0); // 默认草稿状态
        }
        if (bomHeader.getCreatedTime() == null) {
            bomHeader.setCreatedTime(LocalDateTime.now());
        }
        
        // 如果是默认版本，将其他版本设置为非默认
        if (bomHeader.getIsDefault()) {
            List<BomHeaderEntity> existingBoms = bomHeaderRepository.findByMaterialId(bomHeader.getMaterialId());
            for (BomHeaderEntity existingBom : existingBoms) {
                existingBom.setIsDefault(false);
                bomHeaderRepository.save(existingBom);
            }
        }
        
        BomHeaderEntity savedBomHeader = bomHeaderRepository.save(bomHeader);
        return Result.success(savedBomHeader);
    }

    @Override
    public Result<BomHeaderEntity> updateBomHeader(Long id, BomHeaderEntity bomHeader) {
        BomHeaderEntity existingBomHeader = bomHeaderRepository.findById(id).orElse(null);
        if (existingBomHeader == null) {
            return Result.error("BOM头不存在");
        }
        
        bomHeader.setId(id);
        bomHeader.setUpdatedTime(LocalDateTime.now());
        
        // 如果是默认版本，将其他版本设置为非默认
        if (bomHeader.getIsDefault()) {
            List<BomHeaderEntity> existingBoms = bomHeaderRepository.findByMaterialId(bomHeader.getMaterialId());
            for (BomHeaderEntity existingBom : existingBoms) {
                if (!existingBom.getId().equals(id)) {
                    existingBom.setIsDefault(false);
                    bomHeaderRepository.save(existingBom);
                }
            }
        }
        
        BomHeaderEntity updatedBomHeader = bomHeaderRepository.save(bomHeader);
        return Result.success(updatedBomHeader);
    }

    @Override
    public Result<Void> deleteBomHeader(Long id) {
        if (!bomHeaderRepository.existsById(id)) {
            return Result.error("BOM头不存在");
        }
        
        // 删除BOM头前，先删除关联的BOM明细
        List<BomLineEntity> bomLines = bomLineRepository.findByHeaderId(id);
        bomLineRepository.deleteAll(bomLines);
        
        bomHeaderRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<BomHeaderEntity> getBomHeaderById(Long id) {
        BomHeaderEntity bomHeader = bomHeaderRepository.findById(id).orElse(null);
        if (bomHeader == null) {
            return Result.error("BOM头不存在");
        }
        return Result.success(bomHeader);
    }

    @Override
    public Result<List<BomHeaderEntity>> getBomHeadersByMaterialId(Long materialId) {
        try {
            List<BomHeaderEntity> bomHeaders;
            if (materialId == null) {
                bomHeaders = bomHeaderRepository.findAll();
            } else {
                bomHeaders = bomHeaderRepository.findByMaterialId(materialId);
            }
            return Result.success(bomHeaders);
        } catch (Exception e) {
            // 使用日志框架记录错误
            log.error("获取BOM版本列表失败: {}", e.getMessage(), e);
            // 返回明确的错误信息，不使用模拟数据
            return Result.error("获取BOM版本列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getBomVersionsPage(Integer page, Integer size, String bomCode, String productName, Integer status) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (size == null) size = 10;
            int safePage = page <= 0 ? 0 : page - 1;
            int safeSize = size <= 0 ? 10 : size;
            
            // 使用JPA Specifications构建动态查询条件
            Specification<BomHeaderEntity> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // BOM编码筛选
                if (bomCode != null && !bomCode.isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("bomCode"), "%" + bomCode + "%"));
                }
                
                // 产品名称筛选
                if (productName != null && !productName.isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("materialName"), "%" + productName + "%"));
                }
                
                // 状态筛选
                if (status != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            // 执行分页查询
            Page<BomHeaderEntity> bomPage = bomHeaderRepository.findAll(spec, PageRequest.of(safePage, safeSize));
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", bomPage.getContent());
            result.put("total", bomPage.getTotalElements());
            result.put("page", bomPage.getNumber() + 1);
            result.put("size", bomPage.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            // 记录异常信息
            log.error("分页获取BOM版本列表失败: {}", e.getMessage(), e);
            // 返回明确的错误信息，不使用模拟数据
            return Result.error("分页获取BOM版本列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<BomHeaderEntity> getDefaultBomByMaterialId(Long materialId) {
        Optional<BomHeaderEntity> defaultBom = bomHeaderRepository.findByMaterialIdAndIsDefaultTrue(materialId);
        if (defaultBom.isEmpty()) {
            return Result.error("默认BOM不存在");
        }
        return Result.success(defaultBom.get());
    }

    @Override
    public Result<BomLineEntity> addBomLine(BomLineEntity bomLine) {
        BomLineEntity savedBomLine = bomLineRepository.save(bomLine);
        return Result.success(savedBomLine);
    }

    @Override
    public Result<BomLineEntity> updateBomLine(Long id, BomLineEntity bomLine) {
        BomLineEntity existingBomLine = bomLineRepository.findById(id).orElse(null);
        if (existingBomLine == null) {
            return Result.error("BOM子项不存在");
        }
        
        bomLine.setId(id);
        BomLineEntity updatedBomLine = bomLineRepository.save(bomLine);
        return Result.success(updatedBomLine);
    }

    @Override
    public Result<Void> deleteBomLine(Long id) {
        if (!bomLineRepository.existsById(id)) {
            return Result.error("BOM子项不存在");
        }
        
        bomLineRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getBomTree(Long materialId, String version) {
        try {
            // 根据物料ID和版本查询BOM头
            BomHeaderEntity bomHeader;
            if (version != null) {
                // 根据物料ID和版本查询BOM头
                List<BomHeaderEntity> headers = bomHeaderRepository.findByMaterialId(materialId);
                bomHeader = headers.stream()
                    .filter(header -> version.equals(header.getVersion()))
                    .findFirst()
                    .orElse(null);
            } else {
                // 版本为空时，查询默认BOM
                bomHeader = bomHeaderRepository.findByMaterialIdAndIsDefaultTrue(materialId)
                    .orElse(null);
            }
            
            if (bomHeader == null) {
                return Result.error("未找到对应的BOM版本");
            }
            
            // 调用现有方法构建BOM树
            Map<String, Object> bomTree = buildBomTree(bomHeader.getId());
            
            return Result.success(bomTree);
        } catch (Exception e) {
            // 记录异常信息
            log.error("获取BOM树失败: {}", e.getMessage(), e);
            // 返回明确的错误信息
            return Result.error("获取BOM树失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<BomLineEntity>> getBomLinesByHeaderId(Long headerId) {
        List<BomLineEntity> bomLines = bomLineRepository.findByHeaderId(headerId);
        return Result.success(bomLines);
    }

    @Override
    @Transactional
    public Result<List<BomLineEntity>> replaceBomLines(Long headerId, List<BomLineEntity> lines) {
        BomHeaderEntity header = bomHeaderRepository.findById(headerId).orElse(null);
        if (header == null) {
            return Result.error("BOM版本不存在");
        }

        bomLineRepository.deleteByHeaderId(headerId);
        if (lines == null || lines.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        for (BomLineEntity line : lines) {
            line.setId(null);
            line.setHeaderId(headerId);
        }
        List<BomLineEntity> saved = bomLineRepository.saveAll(lines);
        return Result.success(saved);
    }

    @Override
    public Result<Map<String, Object>> getWhereUsed(Long materialId) {
        // 查询所有使用该物料作为子项的BOM明细
        List<BomLineEntity> bomLines = bomLineRepository.findByChildMaterialId(materialId);
        
        // 构建反查结果
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> whereUsedList = new ArrayList<>();
        
        for (BomLineEntity bomLine : bomLines) {
            BomHeaderEntity bomHeader = bomHeaderRepository.findById(bomLine.getHeaderId()).orElse(null);
            if (bomHeader != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("bomHeader", bomHeader);
                item.put("bomLine", bomLine);
                whereUsedList.add(item);
            }
        }
        
        // 注意：字段名不能使用total，否则前端DataTransformer会将响应误判为分页数据并丢弃data字段
        result.put("totalCount", whereUsedList.size());
        result.put("data", whereUsedList);
        
        return Result.success(result);
    }
    
    @Override
    public Result<Map<String, Object>> compareBoms(Long id1, Long id2) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取两个BOM头
            BomHeaderEntity bom1 = bomHeaderRepository.findById(id1).orElse(null);
            BomHeaderEntity bom2 = bomHeaderRepository.findById(id2).orElse(null);
            
            if (bom1 == null || bom2 == null) {
                return Result.error("BOM版本不存在");
            }
            
            // 获取两个BOM的明细
            List<BomLineEntity> lines1 = bomLineRepository.findByHeaderId(id1);
            List<BomLineEntity> lines2 = bomLineRepository.findByHeaderId(id2);
            
            // 比较差异
            List<Map<String, Object>> added = new ArrayList<>();
            List<Map<String, Object>> removed = new ArrayList<>();
            List<Map<String, Object>> changed = new ArrayList<>();
            
            // 创建映射以便快速查找
            Map<Long, BomLineEntity> lines1Map = new HashMap<>();
            for (BomLineEntity line : lines1) {
                lines1Map.put(line.getChildMaterialId(), line);
            }
            
            Map<Long, BomLineEntity> lines2Map = new HashMap<>();
            for (BomLineEntity line : lines2) {
                lines2Map.put(line.getChildMaterialId(), line);
            }
            
            // 查找新增的物料
            for (BomLineEntity line : lines2) {
                if (!lines1Map.containsKey(line.getChildMaterialId())) {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("materialId", line.getChildMaterialId());
                    diff.put("materialCode", line.getChildMaterialCode());
                    diff.put("materialName", line.getChildMaterialName());
                    diff.put("changeType", "added");
                    diff.put("newValue", line);
                    added.add(diff);
                }
            }
            
            // 查找删除的物料
            for (BomLineEntity line : lines1) {
                if (!lines2Map.containsKey(line.getChildMaterialId())) {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("materialId", line.getChildMaterialId());
                    diff.put("materialCode", line.getChildMaterialCode());
                    diff.put("materialName", line.getChildMaterialName());
                    diff.put("changeType", "removed");
                    diff.put("oldValue", line);
                    removed.add(diff);
                }
            }
            
            // 查找修改的物料
            for (Map.Entry<Long, BomLineEntity> entry : lines1Map.entrySet()) {
                Long materialId = entry.getKey();
                BomLineEntity line1 = entry.getValue();
                BomLineEntity line2 = lines2Map.get(materialId);
                
                if (line2 != null) {
                    // 比较所有重要属性
                    boolean isChanged = false;
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("materialId", materialId);
                    diff.put("materialCode", line1.getChildMaterialCode());
                    diff.put("materialName", line1.getChildMaterialName());
                    diff.put("changeType", "changed");
                    diff.put("oldValue", line1);
                    diff.put("newValue", line2);
                    
                    // 比较数量
                    if (!Objects.equals(line1.getQuantity(), line2.getQuantity())) {
                        isChanged = true;
                        diff.put("changedField", "quantity");
                        diff.put("oldQuantity", line1.getQuantity());
                        diff.put("newQuantity", line2.getQuantity());
                    }
                    
                    // 比较单位
                    if (!Objects.equals(line1.getUnit(), line2.getUnit())) {
                        isChanged = true;
                        if (!diff.containsKey("changedField")) {
                            diff.put("changedField", "unit");
                        } else {
                            diff.put("changedField", diff.get("changedField") + ", unit");
                        }
                        diff.put("oldUnit", line1.getUnit());
                        diff.put("newUnit", line2.getUnit());
                    }
                    
                    // 比较层级
                    if (!Objects.equals(line1.getLevel(), line2.getLevel())) {
                        isChanged = true;
                        if (!diff.containsKey("changedField")) {
                            diff.put("changedField", "level");
                        } else {
                            diff.put("changedField", diff.get("changedField") + ", level");
                        }
                        diff.put("oldLevel", line1.getLevel());
                        diff.put("newLevel", line2.getLevel());
                    }
                    
                    // 比较损耗率
                    if (!Objects.equals(line1.getScrapRate(), line2.getScrapRate())) {
                        isChanged = true;
                        if (!diff.containsKey("changedField")) {
                            diff.put("changedField", "scrapRate");
                        } else {
                            diff.put("changedField", diff.get("changedField") + ", scrapRate");
                        }
                        diff.put("oldScrapRate", line1.getScrapRate());
                        diff.put("newScrapRate", line2.getScrapRate());
                    }
                    
                    if (isChanged) {
                        changed.add(diff);
                    }
                }
            }
            
            result.put("bom1", bom1);
            result.put("bom2", bom2);
            result.put("added", added);
            result.put("removed", removed);
            result.put("changed", changed);
            result.put("totalChanges", added.size() + removed.size() + changed.size());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("比较BOM失败: {}", e.getMessage(), e);
            return Result.error("比较BOM失败: " + e.getMessage());
        }
    }

    /**
     * 构建BOM树
     */
    private Map<String, Object> buildBomTree(Long headerId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取BOM头信息
        BomHeaderEntity bomHeader = bomHeaderRepository.findById(headerId).orElse(null);
        if (bomHeader == null) {
            return result;
        }
        
        result.put("bomHeader", bomHeader);
        
        // 获取所有BOM明细
        List<BomLineEntity> allLines = bomLineRepository.findByHeaderId(headerId);
        
        // 按层级分组
        Map<Integer, List<BomLineEntity>> linesByLevel = allLines.stream()
                .collect(Collectors.groupingBy(BomLineEntity::getLevel));
        
        // 构建树结构
        List<Map<String, Object>> rootNodes = new ArrayList<>();
        
        // 处理第一层节点
        List<BomLineEntity> level1Lines = linesByLevel.get(1);
        if (level1Lines != null) {
            for (BomLineEntity line : level1Lines) {
                Map<String, Object> node = buildNode(line, allLines);
                rootNodes.add(node);
            }
        }
        
        result.put("nodes", rootNodes);
        return result;
    }

    /**
     * 递归构建BOM节点
     */
    private Map<String, Object> buildNode(BomLineEntity line, List<BomLineEntity> allLines) {
        Map<String, Object> node = new HashMap<>();
        node.put("line", line);
        
        // 查找子节点
        List<Map<String, Object>> children = new ArrayList<>();
        for (BomLineEntity childLine : allLines) {
            if (childLine.getParentMaterialId() != null && childLine.getParentMaterialId().equals(line.getChildMaterialId())) {
                Map<String, Object> childNode = buildNode(childLine, allLines);
                children.add(childNode);
            }
        }
        
        if (!children.isEmpty()) {
            node.put("children", children);
        }
        
        return node;
    }
}
