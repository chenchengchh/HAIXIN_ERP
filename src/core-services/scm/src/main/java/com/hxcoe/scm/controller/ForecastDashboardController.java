package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.CrmClient;
import com.hxcoe.scm.client.dto.crm.SalesOrderDTO;
import com.hxcoe.scm.client.dto.crm.SalesOrderItemDTO;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.ForecastVersionEntity;
import com.hxcoe.scm.repository.ForecastRepository;
import com.hxcoe.scm.repository.ForecastVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/scm/forecast/dashboard")
public class ForecastDashboardController {

    @Autowired
    private CrmClient crmClient;

    @Autowired
    private ForecastRepository forecastRepository;

    @Autowired
    private ForecastVersionRepository forecastVersionRepository;

    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(@RequestParam String period,
                                                     @RequestParam(defaultValue = "12") Integer months) {
        int m = months == null || months <= 0 ? 12 : months;
        YearMonth end = YearMonth.parse(period);
        List<String> x = new ArrayList<>();
        List<BigDecimal> actual = new ArrayList<>();
        List<BigDecimal> forecast = new ArrayList<>();
        for (int i = m - 1; i >= 0; i--) {
            YearMonth ym = end.minusMonths(i);
            String p = ym.toString();
            x.add(p);
            actual.add(sumActualQty(p));
            forecast.add(sumForecastQty(p));
        }
        Map<String, Object> res = new HashMap<>();
        res.put("period", period);
        res.put("xAxis", x);
        res.put("actual", actual);
        res.put("forecast", forecast);
        return success("趋势分析成功", res);
    }

    @GetMapping("/status-summary")
    public ApiResponse<Map<String, Object>> getStatusSummary(@RequestParam String period) {
        List<ForecastVersionEntity> versions = forecastVersionRepository.findByPeriodOrderByVersionNoDesc(period);
        Map<String, Long> counts = new HashMap<>();
        for (ForecastVersionEntity v : versions) {
            counts.merge(v.getStatus(), 1L, Long::sum);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("period", period);
        res.put("counts", counts);
        return success("状态汇总成功", res);
    }

    @GetMapping("/category-summary")
    public ApiResponse<Map<String, Object>> getCategorySummary(@RequestParam String period,
                                                               @RequestParam(required = false) Long versionId,
                                                               @RequestParam(defaultValue = "5") Integer top) {
        Long resolvedVersionId = resolveVersionId(period, versionId);
        List<DemandForecastEntity> list = resolvedVersionId == null ? List.of() : forecastRepository.findByVersionId(resolvedVersionId);
        Map<String, BigDecimal> byProduct = new HashMap<>();
        for (DemandForecastEntity f : list) {
            BigDecimal v = f.getFinalForecast() == null ? BigDecimal.ZERO : f.getFinalForecast();
            byProduct.merge(f.getProductCode(), v, BigDecimal::add);
        }
        int t = top == null || top <= 0 ? 5 : top;
        List<Map.Entry<String, BigDecimal>> rows = new ArrayList<>(byProduct.entrySet());
        rows.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < Math.min(t, rows.size()); i++) {
            Map.Entry<String, BigDecimal> e = rows.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("value", e.getValue());
            items.add(item);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("period", period);
        res.put("versionId", resolvedVersionId);
        res.put("items", items);
        return success("品类汇总成功", res);
    }

    private Long resolveVersionId(String period, Long versionId) {
        if (versionId != null) return versionId;
        Optional<ForecastVersionEntity> published = forecastVersionRepository.findTopByPeriodAndStatusOrderByVersionNoDesc(period, "PUBLISHED");
        if (published.isPresent()) return published.get().getId();
        return forecastVersionRepository.findTopByPeriodOrderByVersionNoDesc(period).map(ForecastVersionEntity::getId).orElse(null);
    }

    private BigDecimal sumForecastQty(String period) {
        Long versionId = resolveVersionId(period, null);
        if (versionId == null) return BigDecimal.ZERO;
        List<DemandForecastEntity> list = forecastRepository.findByVersionId(versionId);
        BigDecimal sum = BigDecimal.ZERO;
        for (DemandForecastEntity f : list) {
            sum = sum.add(f.getFinalForecast() == null ? BigDecimal.ZERO : f.getFinalForecast());
        }
        return sum;
    }

    private BigDecimal sumActualQty(String period) {
        BigDecimal sum = BigDecimal.ZERO;
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
                    || crmRes.getData() == null || crmRes.getData().getRecords() == null) break;
            List<SalesOrderDTO> records = crmRes.getData().getRecords();
            if (records.isEmpty()) break;
            for (SalesOrderDTO order : records) {
                if (order.getDeliveryDate() == null || order.getItems() == null) continue;
                String orderPeriod = order.getDeliveryDate().getYear() + "-" + String.format("%02d", order.getDeliveryDate().getMonthValue());
                if (!period.equals(orderPeriod)) continue;
                for (SalesOrderItemDTO item : order.getItems()) {
                    if (item == null || item.getQuantity() == null) continue;
                    sum = sum.add(item.getQuantity());
                }
            }
            if (records.size() < size) break;
            page++;
        }
        return sum;
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}
