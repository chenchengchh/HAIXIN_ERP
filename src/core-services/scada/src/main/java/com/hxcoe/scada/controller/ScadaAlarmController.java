package com.hxcoe.scada.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaActiveAlarmEntity;
import com.hxcoe.scada.entity.ScadaAlarmHistoryEntity;
import com.hxcoe.scada.entity.ScadaAlarmTriggerConfigEntity;
import com.hxcoe.scada.repository.ScadaActiveAlarmRepository;
import com.hxcoe.scada.repository.ScadaAlarmHistoryRepository;
import com.hxcoe.scada.repository.ScadaAlarmTriggerConfigRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scada/alarms")
public class ScadaAlarmController {

    private static final long TRIGGER_CONFIG_ID = 1L;

    private final ScadaActiveAlarmRepository activeAlarmRepository;
    private final ScadaAlarmHistoryRepository alarmHistoryRepository;
    private final ScadaAlarmTriggerConfigRepository triggerConfigRepository;
    private final ObjectMapper objectMapper;

    public ScadaAlarmController(
            ScadaActiveAlarmRepository activeAlarmRepository,
            ScadaAlarmHistoryRepository alarmHistoryRepository,
            ScadaAlarmTriggerConfigRepository triggerConfigRepository,
            ObjectMapper objectMapper
    ) {
        this.activeAlarmRepository = activeAlarmRepository;
        this.alarmHistoryRepository = alarmHistoryRepository;
        this.triggerConfigRepository = triggerConfigRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/active")
    public ApiResponse<List<ScadaActiveAlarmEntity>> listActive() {
        return ApiResponse.success(activeAlarmRepository.findAll());
    }

    @PostMapping("/{alarmId}/ack")
    public ApiResponse<ScadaActiveAlarmEntity> acknowledge(@PathVariable String alarmId) {
        ScadaActiveAlarmEntity alarm = activeAlarmRepository.findById(alarmId).orElse(null);
        if (alarm == null) {
            return ApiResponse.error(404, "报警不存在");
        }
        alarm.setStatus("已确认");
        ScadaActiveAlarmEntity saved = activeAlarmRepository.save(alarm);

        ScadaAlarmHistoryEntity history = new ScadaAlarmHistoryEntity();
        history.setTagCode(alarm.getTagCode());
        history.setAlarmType(alarm.getAlarmType());
        history.setSeverity(alarm.getSeverity());
        history.setTriggerTime(alarm.getTriggerTime());
        history.setConfirmTime(LocalDateTime.now());
        alarmHistoryRepository.save(history);

        return ApiResponse.success("确认成功", saved);
    }

    @PostMapping("/{alarmId}/mute")
    public ApiResponse<ScadaActiveAlarmEntity> mute(@PathVariable String alarmId) {
        ScadaActiveAlarmEntity alarm = activeAlarmRepository.findById(alarmId).orElse(null);
        if (alarm == null) {
            return ApiResponse.error(404, "报警不存在");
        }
        alarm.setMuted(Boolean.TRUE);
        ScadaActiveAlarmEntity saved = activeAlarmRepository.save(alarm);
        return ApiResponse.success("消音成功", saved);
    }

    @PostMapping("/{alarmId}/clear")
    public ApiResponse<Void> clear(@PathVariable String alarmId) {
        ScadaActiveAlarmEntity alarm = activeAlarmRepository.findById(alarmId).orElse(null);
        if (alarm == null) {
            return ApiResponse.error(404, "报警不存在");
        }
        activeAlarmRepository.deleteById(alarmId);

        ScadaAlarmHistoryEntity history = new ScadaAlarmHistoryEntity();
        history.setTagCode(alarm.getTagCode());
        history.setAlarmType(alarm.getAlarmType());
        history.setSeverity(alarm.getSeverity());
        history.setTriggerTime(alarm.getTriggerTime());
        history.setRecoveryTime(LocalDateTime.now());
        alarmHistoryRepository.save(history);

        return ApiResponse.success(null);
    }

    @PostMapping("/ack-all")
    public ApiResponse<Void> acknowledgeAll() {
        List<ScadaActiveAlarmEntity> alarms = activeAlarmRepository.findAll();
        for (ScadaActiveAlarmEntity alarm : alarms) {
            if (!"已确认".equals(alarm.getStatus())) {
                alarm.setStatus("已确认");
                activeAlarmRepository.save(alarm);

                ScadaAlarmHistoryEntity history = new ScadaAlarmHistoryEntity();
                history.setTagCode(alarm.getTagCode());
                history.setAlarmType(alarm.getAlarmType());
                history.setSeverity(alarm.getSeverity());
                history.setTriggerTime(alarm.getTriggerTime());
                history.setConfirmTime(LocalDateTime.now());
                alarmHistoryRepository.save(history);
            }
        }
        return ApiResponse.success(null);
    }

    @PostMapping("/clear-all")
    public ApiResponse<Void> clearAll() {
        List<ScadaActiveAlarmEntity> alarms = activeAlarmRepository.findAll();
        for (ScadaActiveAlarmEntity alarm : alarms) {
            ScadaAlarmHistoryEntity history = new ScadaAlarmHistoryEntity();
            history.setTagCode(alarm.getTagCode());
            history.setAlarmType(alarm.getAlarmType());
            history.setSeverity(alarm.getSeverity());
            history.setTriggerTime(alarm.getTriggerTime());
            history.setRecoveryTime(LocalDateTime.now());
            alarmHistoryRepository.save(history);
        }
        activeAlarmRepository.deleteAll();
        return ApiResponse.success(null);
    }

    @GetMapping("/history")
    public ApiResponse<?> history(
            @RequestParam(required = false) String tagCode,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        LocalDateTime start = parseTimeOrNull(startTime);
        LocalDateTime end = parseTimeOrNull(endTime);

        var spec = (org.springframework.data.jpa.domain.Specification<ScadaAlarmHistoryEntity>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tagCode != null && !tagCode.isBlank()) {
                predicates.add(cb.equal(root.get("tagCode"), tagCode));
            }
            if (alarmType != null && !alarmType.isBlank()) {
                predicates.add(cb.equal(root.get("alarmType"), alarmType));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (start != null && end != null) {
                predicates.add(cb.between(root.get("triggerTime"), start, end));
            } else if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("triggerTime"), start));
            } else if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("triggerTime"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size, Sort.by(Sort.Direction.DESC, "id"));
        var p = alarmHistoryRepository.findAll(spec, pageable);
        Map<String, Object> data = new HashMap<>();
        data.put("total", p.getTotalElements());
        data.put("list", p.getContent());
        data.put("page", p.getNumber());
        data.put("size", p.getSize());
        return ApiResponse.success(data);
    }

    @GetMapping("/trigger-config")
    public ApiResponse<Object> getTriggerConfig() {
        ScadaAlarmTriggerConfigEntity entity = triggerConfigRepository.findById(TRIGGER_CONFIG_ID).orElse(null);
        if (entity == null || entity.getConfigJson() == null || entity.getConfigJson().isBlank()) {
            return ApiResponse.success(Map.of());
        }
        try {
            return ApiResponse.success(objectMapper.readValue(entity.getConfigJson(), Object.class));
        } catch (Exception e) {
            return ApiResponse.success(Map.of());
        }
    }

    @PostMapping("/test-trigger")
    public ApiResponse<ScadaActiveAlarmEntity> testTrigger(@RequestBody Map<String, Object> payload) {
        String tagCode = String.valueOf(payload.getOrDefault("tagCode", ""));
        if (tagCode.isBlank()) {
            return ApiResponse.error(400, "点位编码不能为空");
        }

        String triggerValue = String.valueOf(payload.getOrDefault("triggerValue", ""));
        Integer severity = Integer.parseInt(String.valueOf(payload.getOrDefault("severity", "2")));

        ScadaActiveAlarmEntity alarm = new ScadaActiveAlarmEntity();
        alarm.setId(UUID.randomUUID().toString().replace("-", ""));
        alarm.setTagCode(tagCode);
        alarm.setAlarmName("报警触发测试");
        alarm.setAlarmType(String.valueOf(payload.getOrDefault("triggerCondition", "value-exceed")));
        alarm.setSeverity(severity);
        alarm.setStatus("未确认");
        alarm.setTriggerTime(LocalDateTime.now());
        alarm.setCurrentValue(triggerValue.isBlank() ? null : Double.valueOf(triggerValue));
        alarm.setThreshold(null);
        alarm.setDeviceName(String.valueOf(payload.getOrDefault("deviceName", "")));
        alarm.setDescription("报警触发测试");
        alarm.setMuted(Boolean.FALSE);

        ScadaActiveAlarmEntity saved = activeAlarmRepository.save(alarm);

        ScadaAlarmHistoryEntity history = new ScadaAlarmHistoryEntity();
        history.setTagCode(saved.getTagCode());
        history.setAlarmType(saved.getAlarmType());
        history.setSeverity(saved.getSeverity());
        history.setTriggerTime(saved.getTriggerTime());
        alarmHistoryRepository.save(history);

        return ApiResponse.success("触发成功", saved);
    }

    @PutMapping("/trigger-config")
    public ApiResponse<ScadaAlarmTriggerConfigEntity> saveTriggerConfig(@RequestBody Map<String, Object> payload) {
        ScadaAlarmTriggerConfigEntity entity = triggerConfigRepository.findById(TRIGGER_CONFIG_ID).orElseGet(() -> {
            ScadaAlarmTriggerConfigEntity created = new ScadaAlarmTriggerConfigEntity();
            created.setId(TRIGGER_CONFIG_ID);
            return created;
        });
        try {
            entity.setConfigJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            return ApiResponse.error(400, "配置格式无效");
        }
        entity.setUpdatedTime(LocalDateTime.now());
        return ApiResponse.success("保存成功", triggerConfigRepository.save(entity));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(
            @RequestParam(required = false) String date
    ) {
        LocalDate d = parseDateOrToday(date);
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.atTime(23, 59, 59);

        var spec = (org.springframework.data.jpa.domain.Specification<ScadaAlarmHistoryEntity>) (root, query, cb) ->
                cb.between(root.get("triggerTime"), start, end);

        long todayTotal = alarmHistoryRepository.count(spec);
        long activeAlarms = activeAlarmRepository.count();
        long unconfirmed = activeAlarmRepository.findAll().stream().filter(a -> a != null && !"已确认".equals(a.getStatus())).count();

        List<ScadaAlarmHistoryEntity> recent = alarmHistoryRepository.findAll(spec, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "id"))).getContent();
        List<Map<String, Object>> top = recent.stream()
                .filter(x -> x != null && x.getTagCode() != null && !x.getTagCode().isBlank())
                .collect(Collectors.groupingBy(ScadaAlarmHistoryEntity::getTagCode, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("tagCode", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("date", d.toString());
        data.put("todayTotal", todayTotal);
        data.put("activeAlarms", activeAlarms);
        data.put("unconfirmed", unconfirmed);
        data.put("topTags", top);
        return ApiResponse.success(data);
    }

    private LocalDate parseDateOrToday(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ignored) {
            return LocalDate.now();
        }
    }

    private LocalDateTime parseTimeOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
        }
        return null;
    }
}
