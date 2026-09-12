package com.hxcoe.aibrain.service;

import com.hxcoe.aibrain.entity.AiSkillParamEntity;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.repository.AiSkillParamRepository;
import com.hxcoe.aibrain.repository.AiSuggestionRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * AI 技能参数服务（P4-2 反馈学习环核心）
 * 职责：技能阈值参数外置读取（5 分钟缓存）、默认参数注册、每日依据采纳率保守调优
 * 调优规则（防震荡）：
 *   近 30 天样本 ≥10 且拒绝率 >70% → 阈值放宽 10%（减少误报骚扰）
 *   近 30 天样本 ≥10 且采纳率 >80% → 阈值收紧 5%（提高灵敏度）
 */
@Slf4j
@Service
public class SkillParamService {

    /** 缓存有效期：5 分钟 */
    private static final long CACHE_TTL_MS = 300_000L;

    /** 调优最小样本量 */
    private static final int TUNE_MIN_SAMPLES = 10;

    /** 拒绝率上限：超过则放宽阈值 */
    private static final double REJECT_RATE_LIMIT = 0.70;

    /** 采纳率下限：超过则收紧阈值 */
    private static final double ACCEPT_RATE_LIMIT = 0.80;

    /** 默认参数注册表：技能编码 → (参数键 → 默认值) */
    private static final Map<String, Map<String, String>> DEFAULT_PARAMS = Map.of(
            "FAULT_PREDICTION", Map.of("drift_threshold", "0.10"),
            "DELIVERY_RISK", Map.of("overdue_rate_threshold", "0.30"),
            "ENERGY_LOAD", Map.of("peak_load_ratio", "0.85"));

    @Autowired
    private AiSkillParamRepository skillParamRepository;

    @Autowired
    private AiSuggestionRepository suggestionRepository;

    /** 参数缓存：key = skillCode:scope:paramKey */
    private final Map<String, AiSkillParamEntity> cache = new ConcurrentHashMap<>();

    /** 缓存上次刷新时间 */
    private volatile long cacheLoadedAt = 0L;

    /**
     * 启动时注册默认参数（已存在的不覆盖，保证调优成果不丢失）
     */
    @PostConstruct
    public void initDefaults() {
        for (Map.Entry<String, Map<String, String>> skill : DEFAULT_PARAMS.entrySet()) {
            for (Map.Entry<String, String> param : skill.getValue().entrySet()) {
                Optional<AiSkillParamEntity> existing = skillParamRepository
                        .findBySkillCodeAndScopeAndParamKey(skill.getKey(), "GLOBAL", param.getKey());
                if (existing.isEmpty()) {
                    AiSkillParamEntity entity = new AiSkillParamEntity();
                    entity.setSkillCode(skill.getKey());
                    entity.setParamKey(param.getKey());
                    entity.setParamValue(param.getValue());
                    entity.setDefaultValue(param.getValue());
                    entity.setScope("GLOBAL");
                    entity.setAutoTune(true);
                    entity.setUpdatedTime(LocalDateTime.now());
                    entity.setUpdatedBy("INIT");
                    skillParamRepository.save(entity);
                    log.info("技能参数注册: {}.{}={}", skill.getKey(), param.getKey(), param.getValue());
                }
            }
        }
        refreshCache();
    }

    /**
     * 读取双精度参数（GLOBAL 作用域）
     *
     * @param skillCode 技能编码
     * @param paramKey 参数键
     * @param defaultValue 参数缺失时的回退值
     * @return 参数值
     */
    public double getDouble(String skillCode, String paramKey, double defaultValue) {
        AiSkillParamEntity param = getParam(skillCode, paramKey);
        if (param == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(param.getParamValue());
        } catch (NumberFormatException e) {
            log.warn("技能参数解析失败 {}.{}, 值={}, 回退默认 {}", skillCode, paramKey, param.getParamValue(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * 每日 07:00 反馈调优（晨会简报前完成，当天巡检即使用新阈值）
     * 依据近 30 天人工处置统计自动步进阈值，每次调优留审计日志
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void autoTune() {
        try {
            // 1. 统计各技能近 30 天处置分布
            LocalDateTime windowStart = LocalDateTime.now().minusDays(30);
            Map<String, long[]> stats = new HashMap<>(); // type → [accepted+executed, rejected, modified]
            for (AiSuggestionEntity s : suggestionRepository.findAll()) {
                if (s.getCreatedTime() == null || s.getCreatedTime().isBefore(windowStart)) {
                    continue;
                }
                long[] arr = stats.computeIfAbsent(s.getSuggestionType(), k -> new long[3]);
                switch (s.getStatus() == null ? "PENDING" : s.getStatus()) {
                    case "ACCEPTED", "EXECUTED" -> arr[0]++;
                    case "REJECTED" -> arr[1]++;
                    case "MODIFIED" -> arr[2]++;
                    default -> { }
                }
            }

            // 2. 对允许自动调优的参数按保守规则步进
            for (AiSkillParamEntity param : skillParamRepository.findByAutoTuneTrue()) {
                long[] arr = stats.get(param.getSkillCode());
                if (arr == null) {
                    continue;
                }
                long decided = arr[0] + arr[1] + arr[2];
                if (decided < TUNE_MIN_SAMPLES) {
                    continue;
                }
                double current;
                try {
                    current = Double.parseDouble(param.getParamValue());
                } catch (NumberFormatException e) {
                    continue;
                }
                double rejectRate = arr[1] * 1.0 / decided;
                double acceptRate = arr[0] * 1.0 / decided;
                Double next = null;
                String reason;
                if (rejectRate > REJECT_RATE_LIMIT) {
                    // 误报多：放宽阈值（数值上调 10%），减少建议量
                    next = round3(current * 1.10);
                    reason = String.format("拒绝率%.0f%%>70%%，阈值放宽10%%", rejectRate * 100);
                } else if (acceptRate > ACCEPT_RATE_LIMIT) {
                    // 采纳多：收紧阈值（数值下调 5%），提高灵敏度
                    next = round3(current * 0.95);
                    reason = String.format("采纳率%.0f%%>80%%，阈值收紧5%%", acceptRate * 100);
                } else {
                    continue;
                }
                if (next.equals(current)) {
                    continue;
                }
                log.info("AI参数自动调优: {}.{} {}→{}（{}，样本{}）",
                        param.getSkillCode(), param.getParamKey(), current, next, reason, decided);
                param.setParamValue(String.valueOf(next));
                param.setUpdatedTime(LocalDateTime.now());
                param.setUpdatedBy("AUTO_TUNE");
                skillParamRepository.save(param);
            }
            refreshCache();
        } catch (Exception e) {
            log.error("技能参数自动调优失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 强制刷新缓存（调优或人工修改后调用）
     */
    public void refreshCache() {
        List<AiSkillParamEntity> all = skillParamRepository.findAll();
        Map<String, AiSkillParamEntity> fresh = new HashMap<>();
        for (AiSkillParamEntity p : all) {
            fresh.put(cacheKey(p.getSkillCode(), p.getScope(), p.getParamKey()), p);
        }
        cache.clear();
        cache.putAll(fresh);
        cacheLoadedAt = System.currentTimeMillis();
    }

    /**
     * 读取参数实体（带 5 分钟缓存）
     */
    private AiSkillParamEntity getParam(String skillCode, String paramKey) {
        if (System.currentTimeMillis() - cacheLoadedAt > CACHE_TTL_MS) {
            refreshCache();
        }
        return cache.get(cacheKey(skillCode, "GLOBAL", paramKey));
    }

    /**
     * 缓存键拼接
     */
    private String cacheKey(String skillCode, String scope, String paramKey) {
        return skillCode + ":" + (scope == null ? "GLOBAL" : scope) + ":" + paramKey;
    }

    /**
     * 保留三位小数（防浮点尾差）
     */
    private double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
