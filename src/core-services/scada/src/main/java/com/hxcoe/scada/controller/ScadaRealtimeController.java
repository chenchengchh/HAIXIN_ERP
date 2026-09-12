package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
import com.hxcoe.scada.repository.ScadaTagValueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/scada/realtime")
public class ScadaRealtimeController {

    private final ScadaTagValueRepository tagValueRepository;

    public ScadaRealtimeController(ScadaTagValueRepository tagValueRepository) {
        this.tagValueRepository = tagValueRepository;
    }

    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "temperature") String point,
            @RequestParam(defaultValue = "12") int size
    ) {
        String tagCode = normalizePointToTagCode(point);
        int limit = Math.max(1, Math.min(size, 200));
        List<ScadaTagValueEntity> rows = tagValueRepository.findByTagCodeOrderByTsDesc(tagCode, PageRequest.of(0, limit));

        if (rows.isEmpty()) {
            rows = generateAndPersistSamples(tagCode, limit);
        }

        rows.sort(Comparator.comparing(ScadaTagValueEntity::getTs));
        List<Double> values = new ArrayList<>();
        for (ScadaTagValueEntity row : rows) {
            if (row.getValue() != null) {
                values.add(row.getValue());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("tagCode", tagCode);
        data.put("values", values);
        return ApiResponse.success(data);
    }

    @GetMapping("/trend/export")
    public ResponseEntity<byte[]> exportTrend(@RequestParam(defaultValue = "temperature") String point) {
        String tagCode = normalizePointToTagCode(point);
        List<ScadaTagValueEntity> rows = tagValueRepository.findByTagCodeOrderByTsDesc(tagCode, PageRequest.of(0, 200));
        rows.sort(Comparator.comparing(ScadaTagValueEntity::getTs));

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp,value\n");
        for (ScadaTagValueEntity row : rows) {
            sb.append(row.getTs()).append(",");
            sb.append(row.getValue() == null ? "" : row.getValue());
            sb.append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scada_trend_" + tagCode + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * 人工抄表补录（S11 仪表视觉理解配套端点，CONFIRM 级）
     * 现场未联网设备的仪表盘读数经拍照识别/人工核对后，由前端表单确认提交至此端点落库，
     * quality 标记为 "manual" 以区别于自动采集数据。
     *
     * @param body 请求体：tagCode 点位编码（必填）、value 读数（必填，数值）、unit 单位（可选）
     * @return 已保存的读数记录
     */
    @PostMapping("/manual")
    public ApiResponse<Map<String, Object>> createManualReading(@RequestBody Map<String, Object> body) {
        String tagCode = body.get("tagCode") == null ? "" : String.valueOf(body.get("tagCode")).trim().toUpperCase(Locale.ROOT);
        Object rawValue = body.get("value");
        if (tagCode.isBlank()) {
            return ApiResponse.error(400, "tagCode 不能为空");
        }
        Double value;
        try {
            value = rawValue == null ? null : Double.parseDouble(String.valueOf(rawValue).trim());
        } catch (NumberFormatException e) {
            value = null;
        }
        if (value == null) {
            return ApiResponse.error(400, "value 必须为数值");
        }
        String unit = body.get("unit") == null ? null : String.valueOf(body.get("unit")).trim();

        ScadaTagValueEntity entity = new ScadaTagValueEntity();
        entity.setTagCode(tagCode);
        entity.setTs(LocalDateTime.now());
        entity.setValue(value);
        entity.setUnit(unit);
        entity.setQuality("manual");
        ScadaTagValueEntity saved = tagValueRepository.save(entity);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("tagCode", saved.getTagCode());
        data.put("value", saved.getValue());
        data.put("unit", saved.getUnit());
        data.put("ts", saved.getTs());
        data.put("quality", saved.getQuality());
        return ApiResponse.success(data);
    }

    private String normalizePointToTagCode(String point) {
        if (point == null || point.isBlank()) return "TEMP_001";
        String p = point.trim();
        if (p.contains("_")) return p.toUpperCase(Locale.ROOT);
        return switch (p) {
            case "temperature" -> "TEMP_001";
            case "pressure" -> "PRESS_001";
            case "flow" -> "FLOW_001";
            default -> "TEMP_001";
        };
    }

    private List<ScadaTagValueEntity> generateAndPersistSamples(String tagCode, int size) {
        List<ScadaTagValueEntity> samples = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        double base = switch (tagCode) {
            case "PRESS_001" -> 120.0;
            case "FLOW_001" -> 350.0;
            default -> 75.0;
        };
        double spread = switch (tagCode) {
            case "PRESS_001" -> 30.0;
            case "FLOW_001" -> 120.0;
            default -> 20.0;
        };

        for (int i = size - 1; i >= 0; i--) {
            ScadaTagValueEntity entity = new ScadaTagValueEntity();
            entity.setTagCode(tagCode);
            entity.setTs(now.minusMinutes(i * 5L));
            entity.setValue(base + (Math.random() - 0.5) * spread);
            entity.setUnit(switch (tagCode) {
                case "PRESS_001" -> "bar";
                case "FLOW_001" -> "m3/h";
                default -> "°C";
            });
            entity.setQuality("good");
            samples.add(entity);
        }
        return tagValueRepository.saveAll(samples);
    }
}

