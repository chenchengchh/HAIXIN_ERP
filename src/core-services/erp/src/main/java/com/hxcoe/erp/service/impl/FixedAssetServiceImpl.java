package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.FixedAssetEntity;
import com.hxcoe.erp.repository.FixedAssetRepository;
import com.hxcoe.erp.service.FixedAssetService;
import com.hxcoe.common.result.PageResult;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * 固定资产服务实现类
 */
@Service
public class FixedAssetServiceImpl implements FixedAssetService {

    @Autowired
    private FixedAssetRepository fixedAssetRepository;

    /**
     * 创建固定资产
     *
     * @param fixedAssetEntity 固定资产实体
     * @return 创建结果
     */
    @Override
    @Transactional
    public FixedAssetEntity createFixedAsset(FixedAssetEntity fixedAssetEntity) {
        // 生成资产编号
        String assetNo = generateAssetNo(fixedAssetEntity.getAssetCategory());
        fixedAssetEntity.setAssetNo(assetNo);

        // 设置默认值
        if (fixedAssetEntity.getIsDeleted() == null) {
            fixedAssetEntity.setIsDeleted(0);
        }
        if (fixedAssetEntity.getCreatedTime() == null) {
            fixedAssetEntity.setCreatedTime(LocalDateTime.now());
        }
        fixedAssetEntity.setUpdatedTime(LocalDateTime.now());
        if (fixedAssetEntity.getStatus() == null) {
            fixedAssetEntity.setStatus(1); // 默认状态：在用
        }

        // 计算残值
        if (fixedAssetEntity.getExpectedResidualRate() != null && fixedAssetEntity.getOriginalValue() != null) {
            BigDecimal residualValue = fixedAssetEntity.getOriginalValue().multiply(fixedAssetEntity.getExpectedResidualRate());
            fixedAssetEntity.setResidualValue(residualValue);
        }

        // 计算月折旧额
        if (fixedAssetEntity.getOriginalValue() != null && 
            fixedAssetEntity.getResidualValue() != null && 
            fixedAssetEntity.getExpectedUsageYears() != null) {
            BigDecimal monthlyDepreciation = calculateMonthlyDepreciation(
                    fixedAssetEntity.getOriginalValue(),
                    fixedAssetEntity.getResidualValue(),
                    fixedAssetEntity.getExpectedUsageYears()
            );
            fixedAssetEntity.setMonthlyDepreciation(monthlyDepreciation);
        }

        // 初始化累计折旧和净值
        if (fixedAssetEntity.getAccumulatedDepreciation() == null) {
            fixedAssetEntity.setAccumulatedDepreciation(BigDecimal.ZERO);
        }
        if (fixedAssetEntity.getNetValue() == null && fixedAssetEntity.getOriginalValue() != null) {
            fixedAssetEntity.setNetValue(fixedAssetEntity.getOriginalValue());
        }

        // 设置最后折旧日期为采购日期
        if (fixedAssetEntity.getLastDepreciationDate() == null && fixedAssetEntity.getPurchaseDate() != null) {
            fixedAssetEntity.setLastDepreciationDate(fixedAssetEntity.getPurchaseDate());
        }

        return fixedAssetRepository.save(fixedAssetEntity);
    }

    /**
     * 根据ID查询固定资产
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @Override
    public FixedAssetEntity getFixedAssetById(Long id) {
        Optional<FixedAssetEntity> optional = fixedAssetRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * 根据资产编号查询固定资产
     *
     * @param assetNo 资产编号
     * @return 查询结果
     */
    @Override
    public FixedAssetEntity getFixedAssetByNo(String assetNo) {
        return fixedAssetRepository.findByAssetNo(assetNo);
    }

    /**
     * 更新固定资产
     *
     * @param fixedAssetEntity 固定资产实体
     * @return 更新结果
     */
    @Override
    @Transactional
    public FixedAssetEntity updateFixedAsset(FixedAssetEntity fixedAssetEntity) {
        // 检查固定资产是否存在
        FixedAssetEntity existingAsset = fixedAssetRepository.findById(fixedAssetEntity.getId()).orElse(null);
        if (existingAsset == null) {
            return null;
        }

        // 更新基本信息
        existingAsset.setAssetName(fixedAssetEntity.getAssetName());
        existingAsset.setAssetCategory(fixedAssetEntity.getAssetCategory());
        existingAsset.setSpecification(fixedAssetEntity.getSpecification());
        existingAsset.setPurchaseDate(fixedAssetEntity.getPurchaseDate());
        existingAsset.setOriginalValue(fixedAssetEntity.getOriginalValue());
        existingAsset.setExpectedUsageYears(fixedAssetEntity.getExpectedUsageYears());
        existingAsset.setExpectedResidualRate(fixedAssetEntity.getExpectedResidualRate());
        existingAsset.setStatus(fixedAssetEntity.getStatus());
        existingAsset.setUsingDepartment(fixedAssetEntity.getUsingDepartment());
        existingAsset.setCustodian(fixedAssetEntity.getCustodian());
        existingAsset.setLocation(fixedAssetEntity.getLocation());
        existingAsset.setRemark(fixedAssetEntity.getRemark());
        existingAsset.setUpdatedTime(LocalDateTime.now());

        // 重新计算残值
        if (existingAsset.getExpectedResidualRate() != null && existingAsset.getOriginalValue() != null) {
            BigDecimal residualValue = existingAsset.getOriginalValue().multiply(existingAsset.getExpectedResidualRate());
            existingAsset.setResidualValue(residualValue);
        }

        // 重新计算月折旧额
        if (existingAsset.getOriginalValue() != null && 
            existingAsset.getResidualValue() != null && 
            existingAsset.getExpectedUsageYears() != null) {
            BigDecimal monthlyDepreciation = calculateMonthlyDepreciation(
                    existingAsset.getOriginalValue(),
                    existingAsset.getResidualValue(),
                    existingAsset.getExpectedUsageYears()
            );
            existingAsset.setMonthlyDepreciation(monthlyDepreciation);
        }

        // 重新计算净值
        existingAsset.setNetValue(calculateNetValue(
                existingAsset.getOriginalValue(),
                existingAsset.getAccumulatedDepreciation()
        ));

        return fixedAssetRepository.save(existingAsset);
    }

    /**
     * 删除固定资产
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteFixedAsset(Long id) {
        FixedAssetEntity fixedAssetEntity = fixedAssetRepository.findById(id).orElse(null);
        if (fixedAssetEntity != null) {
            // 逻辑删除
            fixedAssetEntity.setIsDeleted(1);
            fixedAssetEntity.setUpdatedTime(LocalDateTime.now());
            fixedAssetRepository.save(fixedAssetEntity);
            return true;
        }
        return false;
    }

    /**
     * 分页查询固定资产列表
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param assetName 资产名称
     * @param assetCategory 资产类别
     * @param status 状态
     * @param usingDepartment 使用部门
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @Override
    public PageResult<FixedAssetEntity> getFixedAssetList(
            Integer page, 
            Integer size, 
            String assetName, 
            String assetCategory, 
            Integer status, 
            String usingDepartment,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        // 创建分页请求
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        // 构建查询条件
        Specification<FixedAssetEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 逻辑删除条件
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            // 资产名称条件（模糊查询）
            if (assetName != null && !assetName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("assetName"), "%" + assetName + "%"));
            }

            // 资产类别条件
            if (assetCategory != null && !assetCategory.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("assetCategory"), assetCategory));
            }

            // 状态条件
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 使用部门条件
            if (usingDepartment != null && !usingDepartment.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("usingDepartment"), usingDepartment));
            }

            // 采购日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("purchaseDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("purchaseDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("purchaseDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<FixedAssetEntity> pageResult = fixedAssetRepository.findAll(specification, pageable);

        // 转换为自定义分页结果
        return PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent()
        );
    }

    /**
     * 生成资产编号
     *
     * @param assetCategory 资产类别
     * @return 资产编号
     */
    @Override
    public String generateAssetNo(String assetCategory) {
        // 资产类别前缀
        String prefix;
        switch (assetCategory) {
            case "房屋建筑":
                prefix = "FW";
                break;
            case "机器设备":
                prefix = "JX";
                break;
            case "运输工具":
                prefix = "YS";
                break;
            case "电子设备":
                prefix = "DZ";
                break;
            case "办公设备":
                prefix = "BG";
                break;
            default:
                prefix = "QT";
                break;
        }

        // 日期格式：YYYYMMDD
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 生成随机数（4位）
        int random = (int) (Math.random() * 9000) + 1000;

        // 组合生成资产编号
        return String.format("%s%s%s", prefix, dateStr, random);
    }

    /**
     * 计算固定资产月折旧额
     * 采用直线折旧法
     *
     * @param originalValue 原值
     * @param residualValue 残值
     * @param expectedUsageYears 预计使用年限
     * @return 月折旧额
     */
    @Override
    public BigDecimal calculateMonthlyDepreciation(BigDecimal originalValue, BigDecimal residualValue, Integer expectedUsageYears) {
        if (originalValue == null || residualValue == null || expectedUsageYears == null || expectedUsageYears <= 0) {
            return BigDecimal.ZERO;
        }

        // 直线折旧法：月折旧额 = (原值 - 残值) / (预计使用年限 * 12)
        BigDecimal depreciableAmount = originalValue.subtract(residualValue);
        BigDecimal totalMonths = new BigDecimal(expectedUsageYears * 12);
        return depreciableAmount.divide(totalMonths, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计提固定资产折旧
     *
     * @param id 固定资产ID
     * @param depreciationDate 折旧日期
     * @return 计提结果
     */
    @Override
    @Transactional
    public FixedAssetEntity calculateDepreciation(Long id, LocalDateTime depreciationDate) {
        FixedAssetEntity fixedAssetEntity = fixedAssetRepository.findById(id).orElse(null);
        if (fixedAssetEntity != null && fixedAssetEntity.getStatus() == 1) { // 只有在用状态的资产才能计提折旧
            // 获取月折旧额
            BigDecimal monthlyDepreciation = fixedAssetEntity.getMonthlyDepreciation() != null ? 
                    fixedAssetEntity.getMonthlyDepreciation() : BigDecimal.ZERO;

            // 更新累计折旧
            BigDecimal accumulatedDepreciation = fixedAssetEntity.getAccumulatedDepreciation() != null ? 
                    fixedAssetEntity.getAccumulatedDepreciation() : BigDecimal.ZERO;
            accumulatedDepreciation = accumulatedDepreciation.add(monthlyDepreciation);
            fixedAssetEntity.setAccumulatedDepreciation(accumulatedDepreciation);

            // 更新净值
            fixedAssetEntity.setNetValue(calculateNetValue(
                    fixedAssetEntity.getOriginalValue(),
                    accumulatedDepreciation
            ));

            // 更新最后折旧日期
            fixedAssetEntity.setLastDepreciationDate(depreciationDate);
            fixedAssetEntity.setUpdatedTime(LocalDateTime.now());

            return fixedAssetRepository.save(fixedAssetEntity);
        }
        return null;
    }

    /**
     * 批量计提固定资产折旧
     *
     * @param assetIds 固定资产ID列表
     * @param depreciationDate 折旧日期
     * @return 折旧结果
     */
    @Override
    @Transactional
    public List<FixedAssetEntity> batchCalculateDepreciation(List<Long> assetIds, LocalDateTime depreciationDate) {
        List<FixedAssetEntity> result = new ArrayList<>();
        for (Long id : assetIds) {
            FixedAssetEntity asset = calculateDepreciation(id, depreciationDate);
            if (asset != null) {
                result.add(asset);
            }
        }
        return result;
    }

    /**
     * 自动计提固定资产折旧
     * 计提所有需要折旧的固定资产
     *
     * @param depreciationDate 折旧日期
     * @return 计提结果
     */
    @Override
    @Transactional
    public List<FixedAssetEntity> autoCalculateDepreciation(LocalDateTime depreciationDate) {
        // 查询所有需要折旧的固定资产：状态为在用且最后折旧日期在当前日期之前
        List<FixedAssetEntity> assetsToDepreciate = fixedAssetRepository.findByStatusAndLastDepreciationDateBefore(1, depreciationDate);
        List<FixedAssetEntity> result = new ArrayList<>();

        for (FixedAssetEntity asset : assetsToDepreciate) {
            // 计算月折旧额
            BigDecimal monthlyDepreciation = asset.getMonthlyDepreciation() != null ? 
                    asset.getMonthlyDepreciation() : BigDecimal.ZERO;

            // 更新累计折旧
            BigDecimal accumulatedDepreciation = asset.getAccumulatedDepreciation() != null ? 
                    asset.getAccumulatedDepreciation() : BigDecimal.ZERO;
            accumulatedDepreciation = accumulatedDepreciation.add(monthlyDepreciation);
            asset.setAccumulatedDepreciation(accumulatedDepreciation);

            // 更新净值
            asset.setNetValue(calculateNetValue(
                    asset.getOriginalValue(),
                    accumulatedDepreciation
            ));

            // 更新最后折旧日期
            asset.setLastDepreciationDate(depreciationDate);
            asset.setUpdatedTime(LocalDateTime.now());

            result.add(asset);
        }

        // 批量保存
        return fixedAssetRepository.saveAll(result);
    }

    /**
     * 固定资产处置
     *
     * @param id 固定资产ID
     * @param disposalDate 处置日期
     * @param disposalReason 处置原因
     * @param disposalAmount 处置金额
     * @return 处置结果
     */
    @Override
    @Transactional
    public FixedAssetEntity disposeFixedAsset(Long id, LocalDateTime disposalDate, String disposalReason, BigDecimal disposalAmount) {
        FixedAssetEntity fixedAssetEntity = fixedAssetRepository.findById(id).orElse(null);
        if (fixedAssetEntity != null) {
            // 更新资产状态为处置
            fixedAssetEntity.setStatus(4);
            fixedAssetEntity.setRemark(disposalReason);
            fixedAssetEntity.setUpdatedTime(LocalDateTime.now());
            return fixedAssetRepository.save(fixedAssetEntity);
        }
        return null;
    }

    /**
     * 固定资产盘点
     *
     * @param assetIds 固定资产ID列表
     * @param inventoryDate 盘点日期
     * @param inventoryPerson 盘点人
     * @return 盘点结果
     */
    @Override
    @Transactional
    public List<FixedAssetEntity> inventoryFixedAssets(List<Long> assetIds, LocalDateTime inventoryDate, String inventoryPerson) {
        List<FixedAssetEntity> result = new ArrayList<>();
        for (Long id : assetIds) {
            FixedAssetEntity asset = fixedAssetRepository.findById(id).orElse(null);
            if (asset != null) {
                // 更新资产信息
                asset.setUpdatedTime(LocalDateTime.now());
                result.add(asset);
            }
        }
        // 批量保存
        return fixedAssetRepository.saveAll(result);
    }

    /**
     * 计算固定资产净值
     *
     * @param originalValue 原值
     * @param accumulatedDepreciation 累计折旧
     * @return 净值
     */
    @Override
    public BigDecimal calculateNetValue(BigDecimal originalValue, BigDecimal accumulatedDepreciation) {
        if (originalValue == null) {
            return BigDecimal.ZERO;
        }
        if (accumulatedDepreciation == null) {
            return originalValue;
        }
        return originalValue.subtract(accumulatedDepreciation);
    }

    /**
     * 统计固定资产数量
     *
     * @param status 状态
     * @return 统计结果
     */
    @Override
    public long countFixedAssets(Integer status) {
        return fixedAssetRepository.countByStatusAndIsDeleted(status, 0);
    }

    /**
     * 获取固定资产类别列表
     *
     * @return 资产类别列表
     */
    @Override
    public List<String> getAssetCategories() {
        List<FixedAssetEntity> allAssets = fixedAssetRepository.findByIsDeleted(0);
        Set<String> categories = new HashSet<>();
        for (FixedAssetEntity asset : allAssets) {
            categories.add(asset.getAssetCategory());
        }
        return new ArrayList<>(categories);
    }

    /**
     * 导出固定资产列表
     *
     * @param params 查询参数
     * @return 导出结果
     */
    @Override
    public byte[] exportFixedAssets(Object params) {
        // 这里简化实现，实际项目中应该根据params构建查询条件，生成excel或其他格式的导出文件
        return new byte[0];
    }
}
