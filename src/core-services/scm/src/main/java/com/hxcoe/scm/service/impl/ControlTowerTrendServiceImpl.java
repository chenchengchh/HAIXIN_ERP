package com.hxcoe.scm.service.impl;

import com.hxcoe.scm.entity.KpiSnapshotEntity;
import com.hxcoe.scm.repository.KpiSnapshotRepository;
import com.hxcoe.scm.service.ControlTowerService;
import com.hxcoe.scm.service.ControlTowerTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ControlTowerTrendServiceImpl implements ControlTowerTrendService {

    @Autowired
    private KpiSnapshotRepository kpiSnapshotRepository;

    @Autowired
    private ControlTowerService controlTowerService;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledSnapshot() {
        snapshotToday();
    }

    @Transactional
    @Override
    public void snapshotToday() {
        LocalDate today = LocalDate.now();
        KpiSnapshotEntity entity = kpiSnapshotRepository.findTopBySnapshotDate(today).orElseGet(KpiSnapshotEntity::new);
        entity.setSnapshotDate(today);
        Map<String, Object> kpis = controlTowerService.getKpis();
        entity.setIntegrationFailedCount(toLong(kpis.get("integrationFailedCount")));
        entity.setMrpReleaseFailedCount(toLong(kpis.get("mrpReleaseFailedCount")));
        entity.setMrpPendingReleaseCount(toLong(kpis.get("mrpPendingReleaseCount")));
        entity.setLowStockRiskCount(toLong(kpis.get("lowStockRiskCount")));
        kpiSnapshotRepository.save(entity);
    }

    @Override
    public List<Map<String, Object>> getKpiTrend(String metric, String from, String to) {
        LocalDate f = LocalDate.parse(from);
        LocalDate t = LocalDate.parse(to);
        List<KpiSnapshotEntity> list = kpiSnapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(f, t);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (KpiSnapshotEntity s : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", s.getSnapshotDate().toString());
            row.put("value", pickMetric(metric, s));
            rows.add(row);
        }
        return rows;
    }

    private Long pickMetric(String metric, KpiSnapshotEntity s) {
        String m = metric == null ? "" : metric;
        return switch (m) {
            case "integrationFailedCount" -> s.getIntegrationFailedCount();
            case "mrpReleaseFailedCount" -> s.getMrpReleaseFailedCount();
            case "mrpPendingReleaseCount" -> s.getMrpPendingReleaseCount();
            case "lowStockRiskCount" -> s.getLowStockRiskCount();
            default -> null;
        };
    }

    private Long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }
}

