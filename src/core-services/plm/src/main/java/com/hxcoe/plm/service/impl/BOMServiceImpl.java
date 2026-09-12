package com.hxcoe.plm.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.entity.BOMEntity;
import com.hxcoe.plm.repository.BOMRepository;
import com.hxcoe.plm.service.BOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class BOMServiceImpl implements BOMService {

    @Autowired
    private BOMRepository bomRepository;

    @Override
    public Result<BOMEntity> createBOM(BOMEntity bom) {
        LocalDateTime now = LocalDateTime.now();
        bom.setCreatedTime(now);
        bom.setUpdatedTime(now);
        if (bom.getStatus() == null || bom.getStatus().trim().isEmpty()) {
            bom.setStatus("DRAFT");
        }
        if (bom.getVersion() == null || bom.getVersion().trim().isEmpty()) {
            bom.setVersion("V1.0");
        }
        if (bom.getBomCode() == null || bom.getBomCode().trim().isEmpty()) {
            bom.setBomCode(generateBomCode(bom.getProductCode(), bom.getVersion()));
        }
        if (bom.getQuantity() == null) {
            bom.setQuantity(BigDecimal.ONE);
        }
        BOMEntity savedBOM = bomRepository.save(bom);
        return Result.success(savedBOM);
    }

    @Override
    public Result<BOMEntity> updateBOM(Long id, BOMEntity bom) {
        BOMEntity existingBOM = bomRepository.findById(id).orElse(null);
        if (existingBOM == null) {
            return Result.error("BOM不存在");
        }
        bom.setId(id);
        bom.setCreatedTime(existingBOM.getCreatedTime());
        bom.setCreatedBy(existingBOM.getCreatedBy());
        if (bom.getBomCode() == null || bom.getBomCode().trim().isEmpty()) {
            bom.setBomCode(existingBOM.getBomCode());
        }
        if (bom.getStatus() == null || bom.getStatus().trim().isEmpty()) {
            bom.setStatus(existingBOM.getStatus());
        }
        if (bom.getVersion() == null || bom.getVersion().trim().isEmpty()) {
            bom.setVersion(existingBOM.getVersion());
        }
        if (bom.getQuantity() == null) {
            bom.setQuantity(existingBOM.getQuantity());
        }
        bom.setUpdatedTime(LocalDateTime.now());
        BOMEntity updatedBOM = bomRepository.save(bom);
        return Result.success(updatedBOM);
    }

    @Override
    public Result<Void> deleteBOM(Long id) {
        if (!bomRepository.existsById(id)) {
            return Result.error("BOM不存在");
        }
        bomRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<BOMEntity> getBOMById(Long id) {
        BOMEntity bom = bomRepository.findById(id).orElse(null);
        if (bom == null) {
            return Result.error("BOM不存在");
        }
        return Result.success(bom);
    }

    @Override
    public Result<List<BOMEntity>> getBOMsByProductId(Long productId, String version) {
        List<BOMEntity> all = bomRepository.findByProductId(productId);
        if (all == null || all.isEmpty()) {
            return Result.success(List.of());
        }

        String targetVersion = version == null || version.trim().isEmpty() ? pickLatestVersion(all) : version.trim();
        List<BOMEntity> filtered = all.stream().filter(b -> Objects.equals(targetVersion, b.getVersion())).toList();
        return Result.success(filtered);
    }

    @Override
    public Result<PageResult<BOMEntity>> getBOMsByPage(Pageable pageable) {
        Page<BOMEntity> page = bomRepository.findAll(pageable);
        PageResult<BOMEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<Void> checkoutByProductId(Long productId) {
        List<BOMEntity> all = bomRepository.findByProductId(productId);
        if (all == null || all.isEmpty()) {
            return Result.error("BOM不存在");
        }
        String latest = pickLatestVersion(all);
        LocalDateTime now = LocalDateTime.now();
        List<BOMEntity> updated = all.stream()
                .filter(b -> Objects.equals(latest, b.getVersion()))
                .map(b -> {
                    b.setStatus("checked_out");
                    b.setUpdatedTime(now);
                    return b;
                })
                .toList();
        bomRepository.saveAll(updated);
        return Result.success();
    }

    @Override
    public Result<Void> checkinByProductId(Long productId) {
        List<BOMEntity> all = bomRepository.findByProductId(productId);
        if (all == null || all.isEmpty()) {
            return Result.error("BOM不存在");
        }

        String latest = pickLatestVersion(all);
        String nextVersion = nextVersion(latest);
        LocalDateTime now = LocalDateTime.now();

        List<BOMEntity> latestLines = all.stream().filter(b -> Objects.equals(latest, b.getVersion())).toList();
        if (latestLines.isEmpty()) {
            return Result.error("BOM不存在");
        }

        List<BOMEntity> newLines = new ArrayList<>();
        for (BOMEntity line : latestLines) {
            BOMEntity copy = new BOMEntity();
            copy.setBomCode(generateBomCode(line.getProductCode(), nextVersion));
            copy.setProductId(line.getProductId());
            copy.setProductCode(line.getProductCode());
            copy.setChildComponentCode(line.getChildComponentCode());
            copy.setMaterialCode(line.getMaterialCode());
            copy.setMaterialName(line.getMaterialName());
            copy.setMaterialSpec(line.getMaterialSpec());
            copy.setUnit(line.getUnit());
            copy.setQuantity(line.getQuantity());
            copy.setLevel(line.getLevel());
            copy.setParentId(line.getParentId());
            copy.setSortOrder(line.getSortOrder());
            copy.setIsKeyPart(line.getIsKeyPart());
            copy.setSubstituteMaterial(line.getSubstituteMaterial());
            copy.setRemark(line.getRemark());
            copy.setStatus("DRAFT");
            copy.setVersion(nextVersion);
            copy.setCreatedBy(line.getUpdatedBy() != null ? line.getUpdatedBy() : line.getCreatedBy());
            copy.setCreatedTime(now);
            copy.setUpdatedBy(line.getUpdatedBy());
            copy.setUpdatedTime(now);
            newLines.add(copy);
        }

        bomRepository.saveAll(newLines);
        return Result.success();
    }

    @Override
    public Result<Void> releaseByProductId(Long productId) {
        List<BOMEntity> all = bomRepository.findByProductId(productId);
        if (all == null || all.isEmpty()) {
            return Result.error("BOM不存在");
        }
        String latest = pickLatestVersion(all);
        LocalDateTime now = LocalDateTime.now();
        List<BOMEntity> updated = all.stream()
                .filter(b -> Objects.equals(latest, b.getVersion()))
                .map(b -> {
                    b.setStatus("released");
                    b.setUpdatedTime(now);
                    return b;
                })
                .toList();
        bomRepository.saveAll(updated);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> compareVersions(Long productId, String fromVersion, String toVersion) {
        if (fromVersion == null || fromVersion.trim().isEmpty() || toVersion == null || toVersion.trim().isEmpty()) {
            return Result.error("版本参数不能为空");
        }

        List<BOMEntity> all = bomRepository.findByProductId(productId);
        if (all == null || all.isEmpty()) {
            return Result.success(Map.of("added", List.of(), "removed", List.of(), "changed", List.of()));
        }

        String from = fromVersion.trim();
        String to = toVersion.trim();
        Map<String, BOMEntity> fromMap = new HashMap<>();
        Map<String, BOMEntity> toMap = new HashMap<>();

        for (BOMEntity b : all) {
            if (Objects.equals(from, b.getVersion())) {
                fromMap.put(keyOf(b), b);
            } else if (Objects.equals(to, b.getVersion())) {
                toMap.put(keyOf(b), b);
            }
        }

        List<BOMEntity> added = new ArrayList<>();
        List<BOMEntity> removed = new ArrayList<>();
        List<Map<String, Object>> changed = new ArrayList<>();

        for (Map.Entry<String, BOMEntity> entry : toMap.entrySet()) {
            if (!fromMap.containsKey(entry.getKey())) {
                added.add(entry.getValue());
            }
        }
        for (Map.Entry<String, BOMEntity> entry : fromMap.entrySet()) {
            if (!toMap.containsKey(entry.getKey())) {
                removed.add(entry.getValue());
            }
        }
        for (Map.Entry<String, BOMEntity> entry : toMap.entrySet()) {
            BOMEntity oldLine = fromMap.get(entry.getKey());
            if (oldLine == null) continue;
            BOMEntity newLine = entry.getValue();
            if (oldLine.getQuantity() != null && newLine.getQuantity() != null && oldLine.getQuantity().compareTo(newLine.getQuantity()) != 0) {
                changed.add(Map.of(
                        "materialCode", newLine.getMaterialCode(),
                        "materialName", newLine.getMaterialName(),
                        "parentId", newLine.getParentId(),
                        "oldQuantity", oldLine.getQuantity(),
                        "newQuantity", newLine.getQuantity()
                ));
            }
        }

        return Result.success(Map.of(
                "added", added,
                "removed", removed,
                "changed", changed,
                "fromVersion", from,
                "toVersion", to
        ));
    }

    private String pickLatestVersion(List<BOMEntity> list) {
        String latest = null;
        int bestScore = Integer.MIN_VALUE;
        for (BOMEntity b : list) {
            String v = b.getVersion();
            int score = versionScore(v);
            if (score > bestScore) {
                bestScore = score;
                latest = v;
            }
        }
        return latest == null ? "V1.0" : latest;
    }

    private int versionScore(String version) {
        if (version == null) return 0;
        String v = version.trim();
        if (v.startsWith("V") || v.startsWith("v")) {
            v = v.substring(1);
        }
        String[] parts = v.split("\\.");
        int major = 0;
        int minor = 0;
        try {
            if (parts.length > 0) major = Integer.parseInt(parts[0]);
            if (parts.length > 1) minor = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {
            return 0;
        }
        return major * 1000 + minor;
    }

    private String nextVersion(String version) {
        if (version == null || version.trim().isEmpty()) return "V1.0";
        String v = version.trim();
        if (v.startsWith("V") || v.startsWith("v")) v = v.substring(1);
        String[] parts = v.split("\\.");
        int major = 1;
        int minor = 0;
        try {
            if (parts.length > 0) major = Integer.parseInt(parts[0]);
            if (parts.length > 1) minor = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {
            return "V1.0";
        }
        minor += 1;
        return "V" + major + "." + minor;
    }

    private String keyOf(BOMEntity b) {
        String parent = b.getParentId() == null ? "root" : String.valueOf(b.getParentId());
        String code = b.getMaterialCode() == null ? "" : b.getMaterialCode();
        return parent + "|" + code;
    }

    private String generateBomCode(String productCode, String version) {
        String p = productCode == null || productCode.trim().isEmpty() ? "PRODUCT" : productCode.trim();
        String v = version == null || version.trim().isEmpty() ? "V1.0" : version.trim();
        return "BOM-" + p + "-" + v + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
