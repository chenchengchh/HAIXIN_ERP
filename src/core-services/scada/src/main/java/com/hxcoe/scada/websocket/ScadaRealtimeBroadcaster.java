package com.hxcoe.scada.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scada.entity.ScadaActiveAlarmEntity;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
import com.hxcoe.scada.repository.ScadaActiveAlarmRepository;
import com.hxcoe.scada.repository.ScadaTagValueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ScadaRealtimeBroadcaster {

    private final ScadaRealtimeWebSocketHandler handler;
    private final ScadaTagValueRepository tagValueRepository;
    private final ScadaActiveAlarmRepository activeAlarmRepository;
    private final ObjectMapper objectMapper;

    public ScadaRealtimeBroadcaster(
            ScadaRealtimeWebSocketHandler handler,
            ScadaTagValueRepository tagValueRepository,
            ScadaActiveAlarmRepository activeAlarmRepository,
            ObjectMapper objectMapper
    ) {
        this.handler = handler;
        this.tagValueRepository = tagValueRepository;
        this.activeAlarmRepository = activeAlarmRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void tick() {
        if (handler.sessionCount() <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        upsertSample("TEMP_001", now, 75.0, 20.0, "°C");
        upsertSample("PRESS_001", now, 120.0, 30.0, "bar");
        upsertSample("FLOW_001", now, 350.0, 120.0, "m3/h");

        List<ScadaTagValueEntity> latestTrend = tagValueRepository.findByTagCodeOrderByTsDesc("TEMP_001", PageRequest.of(0, 12));
        latestTrend.sort(Comparator.comparing(ScadaTagValueEntity::getTs));
        List<Double> values = new ArrayList<>();
        for (ScadaTagValueEntity row : latestTrend) {
            if (row.getValue() != null) values.add(row.getValue());
        }

        List<ScadaActiveAlarmEntity> alarms = activeAlarmRepository.findAll();

        Map<String, Object> trendMsg = new HashMap<>();
        trendMsg.put("type", "trend");
        trendMsg.put("point", "temperature");
        trendMsg.put("values", values);

        Map<String, Object> alarmsMsg = new HashMap<>();
        alarmsMsg.put("type", "alarms");
        alarmsMsg.put("list", alarms);

        broadcastSafely(trendMsg);
        broadcastSafely(alarmsMsg);
    }

    private void upsertSample(String tagCode, LocalDateTime ts, double base, double spread, String unit) {
        ScadaTagValueEntity entity = new ScadaTagValueEntity();
        entity.setTagCode(tagCode);
        entity.setTs(ts);
        entity.setValue(base + (Math.random() - 0.5) * spread);
        entity.setUnit(unit);
        entity.setQuality("good");
        tagValueRepository.save(entity);
    }

    private void broadcastSafely(Map<String, Object> message) {
        try {
            handler.broadcast(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException ignored) {
        }
    }
}

