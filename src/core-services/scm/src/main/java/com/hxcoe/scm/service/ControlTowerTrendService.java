package com.hxcoe.scm.service;

import java.util.List;
import java.util.Map;

public interface ControlTowerTrendService {
    void snapshotToday();

    List<Map<String, Object>> getKpiTrend(String metric, String from, String to);
}

