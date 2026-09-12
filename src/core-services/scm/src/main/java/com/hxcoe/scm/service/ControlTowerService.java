package com.hxcoe.scm.service;

import java.util.Map;
import java.util.List;

public interface ControlTowerService {
    
    /**
     * 获取供应链网络数据 (节点与物流)
     */
    Map<String, Object> getNetworkData();

    Map<String, Object> getKpis();

    List<Map<String, Object>> getAlerts();
}
