package com.hxcoe.scm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.CrmClient;
import com.hxcoe.scm.client.dto.crm.SalesOrderDTO;
import com.hxcoe.scm.client.dto.crm.SalesOrderItemDTO;
import com.hxcoe.scm.entity.ForecastConfigEntity;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.ForecastVersionEntity;
import com.hxcoe.scm.repository.ForecastConfigRepository;
import com.hxcoe.scm.repository.ForecastRepository;
import com.hxcoe.scm.repository.ForecastVersionRepository;
import com.hxcoe.scm.service.ForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ForecastServiceImpl implements ForecastService {

    private static final Logger logger = LoggerFactory.getLogger(ForecastServiceImpl.class);

    @Autowired
    private ForecastRepository forecastRepository;

    @Autowired
    private ForecastVersionRepository forecastVersionRepository;

    @Autowired
    private ForecastConfigRepository forecastConfigRepository;
    
    @Autowired
    private CrmClient crmClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Override
    public List<DemandForecastEntity> generateForecast(String period) {
        Optional<ForecastVersionEntity> latestVersion = forecastVersionRepository.findTopByPeriodOrderByVersionNoDesc(period);
        if (latestVersion.isPresent()) {
            return forecastRepository.findByVersionId(latestVersion.get().getId());
        }
        ForecastVersionEntity version = createVersion(period, 1);
        return createForecastsForVersion(version);
    }

    @Override
    public Page<DemandForecastEntity> getForecastList(Specification<DemandForecastEntity> spec, Pageable pageable) {
        return forecastRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    public DemandForecastEntity updateForecast(Long id, DemandForecastEntity forecast) {
        Optional<DemandForecastEntity> existing = forecastRepository.findById(id);
        if (existing.isPresent()) {
            DemandForecastEntity entity = existing.get();
            if (entity.getVersionId() != null) {
                ForecastVersionEntity v = forecastVersionRepository.findById(entity.getVersionId()).orElse(null);
                if (v != null && "PUBLISHED".equals(v.getStatus())) {
                    throw new IllegalStateException("已发布版本不允许修改");
                }
            }
            entity.setPromotionAdjustment(forecast.getPromotionAdjustment());
            entity.setSeasonalAdjustment(forecast.getSeasonalAdjustment());
            entity.setManualAdjustment(forecast.getManualAdjustment());
            
            // 重新计算最终预测
            // Final = Baseline * (1 + (Promotion + Seasonal + Manual) / 100)
            BigDecimal adjustmentRate = entity.getPromotionAdjustment()
                    .add(entity.getSeasonalAdjustment())
                    .add(entity.getManualAdjustment())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            
            BigDecimal finalForecast = entity.getBaselineForecast()
                    .multiply(BigDecimal.ONE.add(adjustmentRate));
            
            entity.setFinalForecast(finalForecast);
            entity.setUpdateTime(LocalDateTime.now());
            
            return forecastRepository.save(entity);
        }
        return null;
    }

    @Transactional
    @Override
    public void saveForecasts(List<DemandForecastEntity> forecasts) {
        forecastRepository.saveAll(forecasts);
    }

    @Override
    public List<ForecastVersionEntity> getForecastVersions(String period) {
        if (period == null || period.isBlank()) {
            return List.of();
        }
        return forecastVersionRepository.findByPeriodOrderByVersionNoDesc(period);
    }

    @Transactional
    @Override
    public ForecastVersionEntity createForecastVersion(String period, Long fromVersionId) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("period不能为空");
        }
        int nextVersionNo = (int) forecastVersionRepository.countByPeriod(period) + 1;
        ForecastVersionEntity version = createVersion(period, nextVersionNo);

        Long sourceVersionId = fromVersionId;
        if (sourceVersionId == null) {
            sourceVersionId = forecastVersionRepository.findTopByPeriodAndStatusOrderByVersionNoDesc(period, "PUBLISHED")
                    .map(ForecastVersionEntity::getId)
                    .orElseGet(() -> forecastVersionRepository.findTopByPeriodOrderByVersionNoDesc(period)
                            .map(ForecastVersionEntity::getId)
                            .orElse(null));
        }

        if (sourceVersionId != null) {
            List<DemandForecastEntity> sourceForecasts = forecastRepository.findByVersionId(sourceVersionId);
            List<DemandForecastEntity> copied = new ArrayList<>();
            for (DemandForecastEntity src : sourceForecasts) {
                DemandForecastEntity f = new DemandForecastEntity();
                f.setProductCode(src.getProductCode());
                f.setProductName(src.getProductName());
                f.setRegion(src.getRegion());
                f.setPeriod(period);
                f.setVersionId(version.getId());
                f.setVersionNo(version.getVersionNo());
                f.setHistorySales(src.getHistorySales());
                f.setBaselineForecast(src.getBaselineForecast());
                f.setPromotionAdjustment(src.getPromotionAdjustment());
                f.setSeasonalAdjustment(src.getSeasonalAdjustment());
                f.setManualAdjustment(src.getManualAdjustment());
                f.setFinalForecast(src.getFinalForecast());
                f.setStatus("DRAFT");
                copied.add(f);
            }
            forecastRepository.saveAll(copied);
        } else {
            createForecastsForVersion(version);
        }

        return version;
    }

    @Transactional
    @Override
    public ForecastVersionEntity publishForecastVersion(Long versionId) {
        ForecastVersionEntity version = forecastVersionRepository.findById(versionId).orElseThrow(() -> new IllegalArgumentException("版本不存在"));
        if (!"PUBLISHED".equals(version.getStatus())) {
            version.setStatus("PUBLISHED");
            version.setPublishedTime(LocalDateTime.now());
            forecastVersionRepository.save(version);
        }

        List<ForecastVersionEntity> all = forecastVersionRepository.findByPeriodOrderByVersionNoDesc(version.getPeriod());
        for (ForecastVersionEntity v : all) {
            if (!v.getId().equals(version.getId()) && "PUBLISHED".equals(v.getStatus())) {
                v.setStatus("ARCHIVED");
                forecastVersionRepository.save(v);
            }
        }

        List<DemandForecastEntity> forecasts = forecastRepository.findByVersionId(versionId);
        for (DemandForecastEntity f : forecasts) {
            f.setStatus("APPROVED");
        }
        forecastRepository.saveAll(forecasts);
        return version;
    }

    @Transactional
    @Override
    public ForecastVersionEntity rollbackForecastVersion(Long versionId) {
        ForecastVersionEntity version = forecastVersionRepository.findById(versionId).orElseThrow(() -> new IllegalArgumentException("版本不存在"));
        return createForecastVersion(version.getPeriod(), version.getId());
    }

    @Transactional
    @Override
    public List<DemandForecastEntity> updateForecastsByVersion(Long versionId, List<DemandForecastEntity> forecasts) {
        ForecastVersionEntity version = forecastVersionRepository.findById(versionId).orElseThrow(() -> new IllegalArgumentException("版本不存在"));
        if ("PUBLISHED".equals(version.getStatus())) {
            throw new IllegalStateException("已发布版本不允许修改");
        }
        if (forecasts == null || forecasts.isEmpty()) {
            return List.of();
        }
        Map<Long, DemandForecastEntity> existingMap = new HashMap<>();
        for (DemandForecastEntity f : forecastRepository.findByVersionId(versionId)) {
            existingMap.put(f.getId(), f);
        }

        List<DemandForecastEntity> updated = new ArrayList<>();
        for (DemandForecastEntity input : forecasts) {
            if (input.getId() == null) continue;
            DemandForecastEntity entity = existingMap.get(input.getId());
            if (entity == null) continue;
            entity.setPromotionAdjustment(input.getPromotionAdjustment());
            entity.setSeasonalAdjustment(input.getSeasonalAdjustment());
            entity.setManualAdjustment(input.getManualAdjustment());
            BigDecimal adjustmentRate = entity.getPromotionAdjustment()
                    .add(entity.getSeasonalAdjustment())
                    .add(entity.getManualAdjustment())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal finalForecast = entity.getBaselineForecast().multiply(BigDecimal.ONE.add(adjustmentRate));
            entity.setFinalForecast(finalForecast);
            updated.add(entity);
        }
        return forecastRepository.saveAll(updated);
    }

    @Override
    public Map<String, Object> getForecastAccuracy(String period, Long versionId) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("period不能为空");
        }
        ForecastVersionEntity version = null;
        if (versionId != null) {
            version = forecastVersionRepository.findById(versionId).orElse(null);
        }
        if (version == null) {
            version = forecastVersionRepository.findTopByPeriodAndStatusOrderByVersionNoDesc(period, "PUBLISHED")
                    .orElseGet(() -> forecastVersionRepository.findTopByPeriodOrderByVersionNoDesc(period).orElse(null));
        }
        List<DemandForecastEntity> forecasts = version == null ? List.of() : forecastRepository.findByVersionId(version.getId());

        Map<String, BigDecimal> actualByProduct = new HashMap<>();
        int page = 1;
        int size = 100;
        for (int i = 0; i < 20; i++) {
            Result<PageResult<SalesOrderDTO>> crmRes;
            try {
                crmRes = crmClient.getSalesOrders("submitted", page, size);
            } catch (Exception e) {
                break;
            }
            if (crmRes == null || !ResponseStatusAdapter.isSuccess(crmRes.getCode())
                    || crmRes.getData() == null || crmRes.getData().getRecords() == null) {
                break;
            }
            List<SalesOrderDTO> records = crmRes.getData().getRecords();
            if (records.isEmpty()) break;
            for (SalesOrderDTO order : records) {
                if (order.getDeliveryDate() == null) continue;
                String orderPeriod = order.getDeliveryDate().getYear() + "-" + String.format("%02d", order.getDeliveryDate().getMonthValue());
                if (!period.equals(orderPeriod)) continue;
                if (order.getItems() == null) continue;
                for (SalesOrderItemDTO item : order.getItems()) {
                    if (item == null || item.getProductCode() == null) continue;
                    BigDecimal qty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
                    actualByProduct.merge(item.getProductCode(), qty, BigDecimal::add);
                }
            }
            if (records.size() < size) break;
            page++;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal apeSum = BigDecimal.ZERO;
        int apeCount = 0;
        BigDecimal biasSum = BigDecimal.ZERO;
        int biasCount = 0;
        for (DemandForecastEntity f : forecasts) {
            BigDecimal actual = actualByProduct.getOrDefault(f.getProductCode(), BigDecimal.ZERO);
            BigDecimal forecastValue = f.getFinalForecast() == null ? BigDecimal.ZERO : f.getFinalForecast();
            Map<String, Object> row = new HashMap<>();
            row.put("productCode", f.getProductCode());
            row.put("productName", f.getProductName());
            row.put("region", f.getRegion());
            row.put("forecast", forecastValue);
            row.put("actual", actual);
            if (actual.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ape = forecastValue.subtract(actual).abs().divide(actual, 6, RoundingMode.HALF_UP);
                BigDecimal bias = forecastValue.subtract(actual).divide(actual, 6, RoundingMode.HALF_UP);
                row.put("ape", ape);
                row.put("bias", bias);
                apeSum = apeSum.add(ape);
                apeCount++;
                biasSum = biasSum.add(bias);
                biasCount++;
            } else {
                row.put("ape", null);
                row.put("bias", null);
            }
            items.add(row);
        }
        items.sort(Comparator.comparing(m -> String.valueOf(m.get("productCode"))));

        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("versionId", version == null ? null : version.getId());
        result.put("versionNo", version == null ? null : version.getVersionNo());
        result.put("mape", apeCount == 0 ? null : apeSum.divide(BigDecimal.valueOf(apeCount), 6, RoundingMode.HALF_UP));
        result.put("avgBias", biasCount == 0 ? null : biasSum.divide(BigDecimal.valueOf(biasCount), 6, RoundingMode.HALF_UP));
        result.put("items", items);
        return result;
    }

    private ForecastVersionEntity createVersion(String period, int versionNo) {
        ForecastVersionEntity version = new ForecastVersionEntity();
        version.setPeriod(period);
        version.setVersionNo(versionNo);
        version.setStatus("DRAFT");
        return forecastVersionRepository.save(version);
    }

    private ForecastConfigEntity getActiveConfig() {
        return forecastConfigRepository.findTopByEnabledOrderByUpdatedTimeDesc(1).orElseGet(() -> {
            ForecastConfigEntity cfg = new ForecastConfigEntity();
            cfg.setModelType("MOVING_AVERAGE");
            cfg.setHistoryMonths(12);
            cfg.setSmoothingAlpha(null);
            cfg.setEnabled(1);
            return forecastConfigRepository.save(cfg);
        });
    }

    private List<DemandForecastEntity> createForecastsForVersion(ForecastVersionEntity version) {
        ForecastConfigEntity cfg = getActiveConfig();
        YearMonth target = YearMonth.parse(version.getPeriod());
        int historyMonths = cfg.getHistoryMonths() == null ? 12 : cfg.getHistoryMonths();
        List<YearMonth> historyRange = new ArrayList<>();
        for (int i = historyMonths; i >= 1; i--) {
            historyRange.add(target.minusMonths(i));
        }
        YearMonth start = historyRange.get(0);
        YearMonth end = historyRange.get(historyRange.size() - 1);

        Map<String, Map<YearMonth, BigDecimal>> byProduct = new HashMap<>();
        Map<String, String> productNameByCode = new HashMap<>();
        int page = 1;
        int size = 100;
        for (int i = 0; i < 50; i++) {
            Result<PageResult<SalesOrderDTO>> crmRes;
            try {
                crmRes = crmClient.getSalesOrders(null, page, size);
            } catch (Exception e) {
                break;
            }
            if (crmRes == null || !ResponseStatusAdapter.isSuccess(crmRes.getCode())
                    || crmRes.getData() == null || crmRes.getData().getRecords() == null) {
                break;
            }
            List<SalesOrderDTO> records = crmRes.getData().getRecords();
            if (records.isEmpty()) break;
            for (SalesOrderDTO order : records) {
                if (order.getDeliveryDate() == null || order.getItems() == null) continue;
                YearMonth ym = YearMonth.of(order.getDeliveryDate().getYear(), order.getDeliveryDate().getMonthValue());
                if (ym.isBefore(start) || ym.isAfter(end)) continue;
                for (SalesOrderItemDTO item : order.getItems()) {
                    if (item == null || item.getProductCode() == null) continue;
                    if (item.getProductName() != null && !item.getProductName().isBlank()) {
                        productNameByCode.put(item.getProductCode(), item.getProductName());
                    }
                    BigDecimal qty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
                    byProduct.computeIfAbsent(item.getProductCode(), k -> new HashMap<>()).merge(ym, qty, BigDecimal::add);
                }
            }
            if (records.size() < size) break;
            page++;
        }

        List<DemandForecastEntity> forecasts = new ArrayList<>();
        for (Map.Entry<String, Map<YearMonth, BigDecimal>> e : byProduct.entrySet()) {
            String productCode = e.getKey();
            Map<YearMonth, BigDecimal> seriesMap = e.getValue();
            List<BigDecimal> history = new ArrayList<>();
            for (YearMonth ym : historyRange) {
                history.add(seriesMap.getOrDefault(ym, BigDecimal.ZERO));
            }

            BigDecimal baseline = calculateBaseline(cfg, history);
            DemandForecastEntity forecast = new DemandForecastEntity();
            forecast.setProductCode(productCode);
            forecast.setProductName(productNameByCode.getOrDefault(productCode, productCode));
            forecast.setRegion("ALL");
            forecast.setPeriod(version.getPeriod());
            forecast.setVersionId(version.getId());
            forecast.setVersionNo(version.getVersionNo());
            try {
                forecast.setHistorySales(objectMapper.writeValueAsString(history));
            } catch (JsonProcessingException ex) {
                forecast.setHistorySales("[]");
            }
            forecast.setBaselineForecast(baseline);
            forecast.setPromotionAdjustment(BigDecimal.ZERO);
            forecast.setSeasonalAdjustment(BigDecimal.ZERO);
            forecast.setManualAdjustment(BigDecimal.ZERO);
            forecast.setFinalForecast(baseline);
            forecast.setStatus("DRAFT");
            forecasts.add(forecast);
        }
        return forecastRepository.saveAll(forecasts);
    }

    private BigDecimal calculateBaseline(ForecastConfigEntity cfg, List<BigDecimal> history) {
        if (history == null || history.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String model = cfg.getModelType() == null ? "MOVING_AVERAGE" : cfg.getModelType();
        if ("EXP_SMOOTHING".equalsIgnoreCase(model)) {
            BigDecimal alpha = cfg.getSmoothingAlpha();
            if (alpha == null) {
                alpha = new BigDecimal("0.3");
            }
            BigDecimal smoothed = history.get(0);
            for (int i = 1; i < history.size(); i++) {
                BigDecimal x = history.get(i);
                smoothed = alpha.multiply(x).add(BigDecimal.ONE.subtract(alpha).multiply(smoothed));
            }
            return smoothed.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal x : history) {
            sum = sum.add(x == null ? BigDecimal.ZERO : x);
        }
        return sum.divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);
    }
}
