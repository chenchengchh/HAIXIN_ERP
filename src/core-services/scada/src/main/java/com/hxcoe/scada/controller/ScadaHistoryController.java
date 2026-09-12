package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
import com.hxcoe.scada.repository.ScadaTagValueRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/v1/scada/history")
public class ScadaHistoryController {

    private final ScadaTagValueRepository tagValueRepository;

    public ScadaHistoryController(ScadaTagValueRepository tagValueRepository) {
        this.tagValueRepository = tagValueRepository;
    }

    @GetMapping
    public ApiResponse<List<ScadaTagValueEntity>> retrieve(
            @RequestParam String tagCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String interval
    ) {
        String normalizedTag = canonicalTagCode(tagCode);
        LocalDateTime start = parseTimeOrDefault(startTime, LocalDateTime.now().minusDays(1));
        LocalDateTime end = parseTimeOrDefault(endTime, LocalDateTime.now());

        List<ScadaTagValueEntity> rows = tagValueRepository.findByTagCodeAndTsBetweenOrderByTsAsc(normalizedTag, start, end);
        rows = downsample(rows, interval);
        return ApiResponse.success(rows);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam String tagCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String format
    ) {
        if (format != null && !format.isBlank() && !"csv".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("仅支持csv格式导出");
        }
        String normalizedTag = canonicalTagCode(tagCode);
        LocalDateTime start = parseTimeOrDefault(startTime, LocalDateTime.now().minusDays(1));
        LocalDateTime end = parseTimeOrDefault(endTime, LocalDateTime.now());

        List<ScadaTagValueEntity> rows = tagValueRepository.findByTagCodeAndTsBetweenOrderByTsAsc(normalizedTag, start, end);
        rows.sort(Comparator.comparing(ScadaTagValueEntity::getTs));
        rows = downsample(rows, interval);

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp,value,unit,quality\n");
        for (ScadaTagValueEntity row : rows) {
            sb.append(row.getTs()).append(",");
            sb.append(row.getValue() == null ? "" : row.getValue()).append(",");
            sb.append(row.getUnit() == null ? "" : row.getUnit()).append(",");
            sb.append(row.getQuality() == null ? "" : row.getQuality());
            sb.append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scada_history_" + normalizedTag + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String canonicalTagCode(String raw) {
        String v = raw == null ? "" : raw.trim();
        if (v.isBlank()) return "";
        return switch (v.toLowerCase(Locale.ROOT)) {
            case "temperature" -> "TEMP_001";
            case "pressure" -> "PRESS_001";
            case "flow" -> "FLOW_001";
            default -> v.toUpperCase(Locale.ROOT);
        };
    }

    private List<ScadaTagValueEntity> downsample(List<ScadaTagValueEntity> rows, String interval) {
        if (interval == null || interval.isBlank()) return rows;
        String v = interval.trim().toLowerCase(Locale.ROOT);
        if ("raw".equals(v)) return rows;
        Duration bucketSize = parseInterval(v);
        if (bucketSize == null) return rows;

        Map<LocalDateTime, Agg> bucket = new TreeMap<>();
        for (ScadaTagValueEntity row : rows) {
            if (row == null || row.getTs() == null) continue;
            LocalDateTime key = floorTime(row.getTs(), bucketSize);
            Agg agg = bucket.computeIfAbsent(key, k -> new Agg());
            agg.count++;
            if (row.getValue() != null) {
                agg.sum += row.getValue();
                agg.valueCount++;
            }
            if (agg.unit == null && row.getUnit() != null) agg.unit = row.getUnit();
            if (agg.quality == null && row.getQuality() != null) agg.quality = row.getQuality();
            if (agg.tagCode == null && row.getTagCode() != null) agg.tagCode = row.getTagCode();
        }

        return bucket.entrySet().stream().map(e -> {
            Agg agg = e.getValue();
            ScadaTagValueEntity out = new ScadaTagValueEntity();
            out.setTagCode(agg.tagCode);
            out.setTs(e.getKey());
            out.setUnit(agg.unit);
            out.setQuality(agg.quality == null ? "good" : agg.quality);
            out.setValue(agg.valueCount == 0 ? null : agg.sum / agg.valueCount);
            return out;
        }).toList();
    }

    private Duration parseInterval(String interval) {
        if (interval == null || interval.isBlank()) return null;
        if (interval.endsWith("m")) {
            long n = parseLong(interval.substring(0, interval.length() - 1));
            return n <= 0 ? null : Duration.ofMinutes(n);
        }
        if (interval.endsWith("h")) {
            long n = parseLong(interval.substring(0, interval.length() - 1));
            return n <= 0 ? null : Duration.ofHours(n);
        }
        if (interval.endsWith("d")) {
            long n = parseLong(interval.substring(0, interval.length() - 1));
            return n <= 0 ? null : Duration.ofDays(n);
        }
        return null;
    }

    private long parseLong(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return -1;
        }
    }

    private LocalDateTime floorTime(LocalDateTime t, Duration bucket) {
        long seconds = bucket.getSeconds();
        if (seconds <= 0) return t;
        long epoch = t.toEpochSecond(OffsetDateTime.now().getOffset());
        long floored = (epoch / seconds) * seconds;
        return LocalDateTime.ofEpochSecond(floored, 0, OffsetDateTime.now().getOffset());
    }

    private LocalDateTime parseTimeOrDefault(String raw, LocalDateTime defaultValue) {
        if (raw == null || raw.isBlank()) return defaultValue;
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    private static class Agg {
        int count;
        int valueCount;
        double sum;
        String unit;
        String quality;
        String tagCode;
    }
}
