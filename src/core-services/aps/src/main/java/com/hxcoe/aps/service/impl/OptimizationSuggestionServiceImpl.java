package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.OptimizationSuggestionEntity;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.entity.ScheduleDetailEntity;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.repository.OptimizationSuggestionRepository;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.aps.repository.ScheduleDetailRepository;
import com.hxcoe.aps.repository.ScheduleResultRepository;
import com.hxcoe.aps.service.OptimizationSuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OptimizationSuggestionServiceImpl implements OptimizationSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationSuggestionServiceImpl.class);

    /** 负载偏差阈值：并行资源数量分配比例与产能比例偏差超过该值时给出负载均衡建议 */
    private static final double LOAD_IMBALANCE_THRESHOLD = 0.15;

    /** 高负荷阈值：单资源工作时长占排程总时长比例超过该值时给出瓶颈提示 */
    private static final double HIGH_LOAD_THRESHOLD = 0.9;

    @Autowired
    private OptimizationSuggestionRepository optimizationSuggestionRepository;

    @Autowired
    private ScheduleResultRepository scheduleResultRepository;

    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Autowired
    private com.hxcoe.aps.repository.ResourceRepository resourceRepository;

    @Override
    public List<OptimizationSuggestionEntity> getOptimizationSuggestions(Long scheduleResultId, String type, String status) {
        if (scheduleResultId != null) {
            if (type != null && status != null) {
                return optimizationSuggestionRepository.findByScheduleResultIdAndTypeAndStatus(scheduleResultId, type, status);
            } else if (type != null) {
                return optimizationSuggestionRepository.findByScheduleResultIdAndType(scheduleResultId, type);
            } else if (status != null) {
                return optimizationSuggestionRepository.findByScheduleResultIdAndStatus(scheduleResultId, status);
            } else {
                return optimizationSuggestionRepository.findByScheduleResultId(scheduleResultId);
            }
        }
        // scheduleResultId为空时支持全量查询（动态优化页面按类型/状态筛选）
        if (type != null && status != null) {
            return optimizationSuggestionRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            return optimizationSuggestionRepository.findByType(type);
        } else if (status != null) {
            return optimizationSuggestionRepository.findByStatus(status);
        } else {
            return optimizationSuggestionRepository.findAll();
        }
    }

    @Override
    public Page<OptimizationSuggestionEntity> getOptimizationSuggestionsByPage(Pageable pageable) {
        return optimizationSuggestionRepository.findAll(pageable);
    }

    /**
     * 重新分析排程结果并生成优化建议（真实分析逻辑，非查库返回）：
     * 1. 瓶颈工序检测：按工序组（相同起止时间）统计耗时，耗时最长的工序标记为瓶颈；
     * 2. 负载均衡检测：同工序并行资源的数量分配比例偏差过大时给出调整建议；
     * 3. 高负荷检测：单资源工作时长占排程总时长超90%时提示产能风险；
     * 4. 超期检测：排程结束时间晚于计划结束时间时给出交期风险建议。
     * 幂等处理：先清除该结果状态为NEW的旧建议（保留已采纳ACCEPTED/已忽略IGNORED），再写入新建议。
     */
    @Transactional
    @Override
    public List<OptimizationSuggestionEntity> reanalyzeSchedule(Long scheduleResultId) {
        Optional<ScheduleResultEntity> resultOpt = scheduleResultRepository.findById(scheduleResultId);
        if (resultOpt.isEmpty()) {
            logger.warn("重新分析失败，排程结果不存在: {}", scheduleResultId);
            return List.of();
        }
        ScheduleResultEntity result = resultOpt.get();
        List<ScheduleDetailEntity> details = scheduleDetailRepository.findByScheduleResultId(scheduleResultId);
        if (details.isEmpty()) {
            logger.warn("重新分析失败，排程结果无详情: {}", scheduleResultId);
            return List.of();
        }

        List<OptimizationSuggestionEntity> newSuggestions = new ArrayList<>();

        // 1. 瓶颈工序检测：按(开始时间,结束时间)分组视为同一工序，找耗时最长组
        Map<String, List<ScheduleDetailEntity>> processGroups = new LinkedHashMap<>();
        details.stream()
                .filter(d -> d.getStartTime() != null && d.getEndTime() != null)
                .sorted(Comparator.comparing(ScheduleDetailEntity::getStartTime))
                .forEach(d -> processGroups
                        .computeIfAbsent(d.getStartTime() + "|" + d.getEndTime(), k -> new ArrayList<>()).add(d));

        long maxProcessMinutes = 0;
        List<ScheduleDetailEntity> bottleneckGroup = null;
        for (List<ScheduleDetailEntity> group : processGroups.values()) {
            ScheduleDetailEntity first = group.get(0);
            long minutes = Duration.between(first.getStartTime(), first.getEndTime()).toMinutes();
            if (minutes > maxProcessMinutes) {
                maxProcessMinutes = minutes;
                bottleneckGroup = group;
            }
        }
        if (bottleneckGroup != null && processGroups.size() > 1) {
            String resources = bottleneckGroup.stream()
                    .map(ScheduleDetailEntity::getResourceName).distinct()
                    .reduce((a, b) -> a + "、" + b).orElse("");
            final long bottleneckMinutes = maxProcessMinutes;
            newSuggestions.add(buildSuggestion(scheduleResultId, "bottleneck",
                    String.format("工序(%s)耗时%d分钟，为全线最长瓶颈工序", resources, bottleneckMinutes),
                    "建议增加该工序并行设备或优化加工节拍，缩短整体完工时间", "HIGH"));
        }

        // 2. 负载均衡检测：同工序并行资源的"数量/产能"负载时长偏差过大(>阈值)时才提示。
        // 按产能比例分配时各资源负载时长一致，属于均衡；仅当人工调整或异常分配导致偏差才报警。
        for (List<ScheduleDetailEntity> group : processGroups.values()) {
            if (group.size() < 2) {
                continue;
            }
            double maxLoad = 0;
            double minLoad = Double.MAX_VALUE;
            for (ScheduleDetailEntity d : group) {
                double capacity = resourceRepository.findById(d.getResourceId())
                        .map(r -> r.getCapacity() != null && r.getCapacity() > 0 ? r.getCapacity() : 10)
                        .orElse(10);
                double load = d.getQuantity().doubleValue() / capacity;
                maxLoad = Math.max(maxLoad, load);
                minLoad = Math.min(minLoad, load);
            }
            if (minLoad > 0 && maxLoad / minLoad > 1 + LOAD_IMBALANCE_THRESHOLD) {
                String imbalance = group.stream()
                        .map(d -> d.getResourceName() + ":" + d.getQuantity())
                        .reduce((a, b) -> a + "，" + b).orElse("");
                newSuggestions.add(buildSuggestion(scheduleResultId, "bottleneck",
                        "并行资源负载分配不均：" + imbalance,
                        "建议按设备实际产能重新分配任务数量，平衡各资源负载", "MEDIUM"));
            }
        }

        // 3. 高负荷检测：资源累计工作时长 / 排程总时长 > 90%
        LocalDateTime scheduleStart = details.stream().map(ScheduleDetailEntity::getStartTime)
                .min(Comparator.naturalOrder()).orElse(null);
        LocalDateTime scheduleEnd = details.stream().map(ScheduleDetailEntity::getEndTime)
                .max(Comparator.naturalOrder()).orElse(null);
        if (scheduleStart != null && scheduleEnd != null) {
            long totalMinutes = Math.max(1, Duration.between(scheduleStart, scheduleEnd).toMinutes());
            Map<Long, Long> resourceMinutes = new LinkedHashMap<>();
            Map<Long, String> resourceNames = new LinkedHashMap<>();
            for (ScheduleDetailEntity d : details) {
                long minutes = Duration.between(d.getStartTime(), d.getEndTime()).toMinutes();
                resourceMinutes.merge(d.getResourceId(), minutes, Long::sum);
                resourceNames.put(d.getResourceId(), d.getResourceName());
            }
            for (Map.Entry<Long, Long> entry : resourceMinutes.entrySet()) {
                double loadRate = entry.getValue() * 1.0 / totalMinutes;
                if (loadRate > HIGH_LOAD_THRESHOLD) {
                    newSuggestions.add(buildSuggestion(scheduleResultId, "equipmentFailure",
                            String.format("资源[%s]负荷率达%.0f%%，排程期内几乎无空闲", resourceNames.get(entry.getKey()), loadRate * 100),
                            "建议预留设备维护窗口或分流部分任务至其他资源，降低故障停线风险", "MEDIUM"));
                }
            }
        }

        // 4. 超期检测：排程结束时间晚于计划结束时间
        Optional<ProductionPlanEntity> planOpt = productionPlanRepository.findById(result.getPlanId());
        if (planOpt.isPresent() && planOpt.get().getEndTime() != null && scheduleEnd != null
                && scheduleEnd.isAfter(planOpt.get().getEndTime())) {
            long delayHours = Duration.between(planOpt.get().getEndTime(), scheduleEnd).toHours();
            newSuggestions.add(buildSuggestion(scheduleResultId, "orderChange",
                    String.format("排程完工时间超出计划交期%d小时", delayHours),
                    "建议压缩瓶颈工序工时、增加并行资源或与客户协商调整交期", "HIGH"));
        }

        // 幂等写入：清除该结果未处理的旧建议，保留已采纳/已忽略的历史；
        // 同描述建议去重（多工序组共用同一批资源时检测结论相同，按描述分组仅保留首次出现的一条）
        optimizationSuggestionRepository.deleteByScheduleResultIdAndStatus(scheduleResultId, "NEW");
        Map<String, OptimizationSuggestionEntity> distinctMap = new LinkedHashMap<>();
        for (OptimizationSuggestionEntity s : newSuggestions) {
            distinctMap.putIfAbsent(s.getType() + "|" + s.getDescription(), s);
        }
        List<OptimizationSuggestionEntity> distinctSuggestions = new ArrayList<>(distinctMap.values());
        if (!distinctSuggestions.isEmpty()) {
            optimizationSuggestionRepository.saveAll(distinctSuggestions);
        }
        logger.info("排程结果{}重新分析完成，生成{}条优化建议", scheduleResultId, distinctSuggestions.size());
        return optimizationSuggestionRepository.findByScheduleResultId(scheduleResultId);
    }

    /**
     * 构建优化建议实体（初始状态NEW）
     */
    private OptimizationSuggestionEntity buildSuggestion(Long scheduleResultId, String type,
                                                         String description, String suggestion, String priority) {
        OptimizationSuggestionEntity entity = new OptimizationSuggestionEntity();
        entity.setScheduleResultId(scheduleResultId);
        entity.setType(type);
        entity.setDescription(description);
        entity.setSuggestion(suggestion);
        entity.setPriority(priority);
        entity.setStatus("NEW");
        return entity;
    }

    @Override
    public OptimizationSuggestionEntity createOptimizationSuggestion(OptimizationSuggestionEntity suggestion) {
        return optimizationSuggestionRepository.save(suggestion);
    }

    @Override
    public OptimizationSuggestionEntity updateOptimizationSuggestion(Long id, OptimizationSuggestionEntity suggestion) {
        suggestion.setId(id);
        return optimizationSuggestionRepository.save(suggestion);
    }

    @Override
    public void deleteOptimizationSuggestion(Long id) {
        optimizationSuggestionRepository.deleteById(id);
    }

    @Override
    public OptimizationSuggestionEntity acceptSuggestion(Long id) {
        OptimizationSuggestionEntity suggestion = optimizationSuggestionRepository.findById(id).orElse(null);
        if (suggestion != null) {
            suggestion.setStatus("ACCEPTED");
            return optimizationSuggestionRepository.save(suggestion);
        }
        return null;
    }

    @Override
    public OptimizationSuggestionEntity ignoreSuggestion(Long id) {
        OptimizationSuggestionEntity suggestion = optimizationSuggestionRepository.findById(id).orElse(null);
        if (suggestion != null) {
            suggestion.setStatus("IGNORED");
            return optimizationSuggestionRepository.save(suggestion);
        }
        return null;
    }

    @Override
    public OptimizationSuggestionEntity resetSuggestionStatus(Long id) {
        OptimizationSuggestionEntity suggestion = optimizationSuggestionRepository.findById(id).orElse(null);
        if (suggestion != null) {
            suggestion.setStatus("NEW");
            return optimizationSuggestionRepository.save(suggestion);
        }
        return null;
    }

    @Override
    public List<OptimizationSuggestionEntity> getSuggestionReport(Long scheduleResultId) {
        return optimizationSuggestionRepository.findByScheduleResultId(scheduleResultId);
    }
}
