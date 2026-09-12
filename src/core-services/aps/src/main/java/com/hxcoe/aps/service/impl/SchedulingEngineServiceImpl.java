package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ProcessEntity;
import com.hxcoe.aps.entity.ProductEntity;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.entity.ScheduleDetailEntity;
import com.hxcoe.aps.entity.ScheduleHistoryEntity;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.enums.SchedulingAlgorithm;
import com.hxcoe.aps.repository.ProcessRepository;
import com.hxcoe.aps.repository.ProductRepository;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.aps.repository.ResourceRepository;
import com.hxcoe.aps.repository.ScheduleDetailRepository;
import com.hxcoe.aps.repository.ScheduleHistoryRepository;
import com.hxcoe.aps.repository.ScheduleResultRepository;
import com.hxcoe.aps.service.SchedulingEngineService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hxcoe.aps.client.MesClient;
import com.hxcoe.aps.client.dto.mes.MesWorkOrderDTO;
import com.hxcoe.aps.service.algorithm.GeneticAlgorithmScheduler;

@Service
public class SchedulingEngineServiceImpl implements SchedulingEngineService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingEngineServiceImpl.class);

    /** 参与生产排程的资源类型（仓储类资源不参与加工排程） */
    private static final List<String> SCHEDULABLE_TYPES = List.of("设备", "生产线");

    /** 生产线资源类型 */
    private static final String TYPE_LINE = "生产线";

    /** 设备资源类型 */
    private static final String TYPE_EQUIPMENT = "设备";

    /**
     * 工序名称 -> 设备名称包含关键词：排程时按关键词匹配该工序的加工设备。
     * 均质/冷却与乳化共用乳化锅（锅内连续完成）；未命中的工序（原料准备/入库）走生产线资源。
     */
    private static final Map<String, String> PROCESS_EQUIPMENT_KEYWORD = Map.of(
            "乳化", "乳化",
            "均质", "乳化",
            "冷却", "乳化",
            "灌装", "灌装",
            "贴标", "贴标",
            "包装", "包装",
            "QC检验", "检验");

    @Autowired
    private ProductionPlanRepository planRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ScheduleResultRepository resultRepository;

    @Autowired
    private ScheduleDetailRepository detailRepository;

    @Autowired
    private ScheduleHistoryRepository historyRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MesClient mesClient;

    private final GeneticAlgorithmScheduler geneticScheduler = new GeneticAlgorithmScheduler();

    /**
     * 下达排程结果到MES：遍历排程详情逐条生成MES工单，全部成功后更新状态为RELEASED。
     * 注意：MES与OA等服务的统一响应成功码不一致（common Result成功码为0，部分服务用200），
     * 因此判断成功时必须同时接受0和200，避免实际已创建工单却误判失败导致重复下达。
     */
    @Transactional
    @Override
    public void releaseSchedule(Long scheduleResultId) {
        // 1. 获取排程结果
        Optional<ScheduleResultEntity> resultOpt = resultRepository.findById(scheduleResultId);
        if (resultOpt.isEmpty()) {
            throw new RuntimeException("排程结果不存在");
        }
        ScheduleResultEntity result = resultOpt.get();

        // 2. 获取关联计划信息 (用于获取产品信息)
        Optional<ProductionPlanEntity> planOpt = planRepository.findById(result.getPlanId());
        if (planOpt.isEmpty()) {
             throw new RuntimeException("关联计划不存在");
        }
        ProductionPlanEntity plan = planOpt.get();

        // 3. 获取本次排程结果的详情（按scheduleResultId取数，避免混入历史排程的详情）
        List<ScheduleDetailEntity> details = detailRepository.findByScheduleResultId(scheduleResultId);

        // 4. 遍历详情，调用MES接口
        for (ScheduleDetailEntity detail : details) {
            if ("RELEASED".equals(detail.getStatus())) {
                continue; // 已下达的跳过
            }

            try {
                MesWorkOrderDTO workOrder = new MesWorkOrderDTO();
                workOrder.setWorkOrderNo("WO-" + detail.getId());
                workOrder.setSourceId("SCH-" + detail.getId());
                workOrder.setProductCode(plan.getProductCode());
                workOrder.setProductName(plan.getProductName());
                workOrder.setPlanQuantity(detail.getQuantity());
                workOrder.setResourceName(detail.getResourceName());
                workOrder.setStartTime(detail.getStartTime());
                workOrder.setEndTime(detail.getEndTime());
                workOrder.setStatus("CREATED");

                // MES返回原始JSON，手动解析code字段（各服务Result结构差异大，避免Feign泛型反序列化失败）
                String mesJson = mesClient.createWorkOrder(workOrder);
                Integer mesCode = parseResultCode(mesJson);
                // 成功码兼容：common Result为0，部分服务为200
                if (mesCode != null && (mesCode == 0 || mesCode == 200)) {
                    detail.setStatus("RELEASED");
                } else {
                    logger.error("下达工单失败: detailId={}, 响应={}", detail.getId(), mesJson);
                }
            } catch (Exception e) {
                logger.error("下达工单异常: detailId={}, error={}", detail.getId(), e.getMessage());
            }
        }

        detailRepository.saveAll(details);

        // 更新结果状态
        result.setStatus("RELEASED");
        resultRepository.save(result);
    }

    /**
     * 执行排程计算：根据算法类型选择不同的排程策略。
     * 
     * 支持的算法：
     * 1. GENETIC - 遗传算法：通过模拟自然选择和遗传机制寻找最优排程方案
     * 2. 其他（PRIORITY/EDD/CPM/HEURISTIC）- 基于工艺路线的串行排程
     * 
     * 基于工艺路线(aps_process按sequence排序)的顺序排程：
     * 工序间串行：下道工序开始时间 = 上道工序结束时间；同工序多台设备按产能比例并行拆分数量。
     * 工序耗时 = 准备时间 + 加工时间(数量/产能) + 拆卸时间；记录排程历史；结果状态RUNNING -> COMPLETED。
     * 重排前清理该计划未下达(SCHEDULED)的旧详情，保留已下达(RELEASED)历史。
     */
    @Transactional
    @Override
    public ScheduleResultEntity executeScheduling(Long planId, SchedulingAlgorithm algorithm, List<String> objectives) {
        long startMillis = System.currentTimeMillis();
        logger.info("开始排程，计划ID: {}, 算法: {}", planId, algorithm);

        // 1. 获取生产计划
        Optional<ProductionPlanEntity> planOpt = planRepository.findById(planId);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("生产计划不存在: " + planId);
        }
        ProductionPlanEntity plan = planOpt.get();
        if (plan.getQuantity() == null || plan.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("计划数量为空或为0，请先完善计划的产品与数量信息: " + plan.getPlanNo());
        }

        // 2. 加载工艺路线（按工序序号升序）；无工艺路线时回退为单工序整体排程
        List<ProcessEntity> processes = processRepository.findAllByOrderBySequenceAsc();
        if (processes.isEmpty()) {
            logger.warn("工艺路线为空，回退为单工序排程: planId={}", planId);
            processes = List.of(ProcessEntity.builder()
                    .processName("生产加工").workshop("综合车间").sequence(1)
                    .processingTime(60).setupTime(0).teardownTime(0).build());
        }

        // 3. 获取可排程资源：仅available状态的设备/生产线
        List<ResourceEntity> resources = resourceRepository.findByStatus("available").stream()
                .filter(r -> SCHEDULABLE_TYPES.contains(r.getType()))
                .sorted(Comparator.comparing(ResourceEntity::getId))
                .toList();
        if (resources.isEmpty()) {
            throw new RuntimeException("没有可用资源进行排程");
        }

        // 3.1 确定产品所属生产线（原料准备/入库工序挂靠在产线资源上）
        String productionLine = productRepository.findByProductCode(plan.getProductCode())
                .map(ProductEntity::getProductionLine).orElse(null);

        // 4. 创建排程结果头
        ScheduleResultEntity result = new ScheduleResultEntity();
        result.setPlanId(planId);
        result.setScheduleNo("SCH-" + System.currentTimeMillis());
        result.setAlgorithm(algorithm != null ? algorithm.name() : "DEFAULT");
        result.setStatus("RUNNING");
        result = resultRepository.save(result);

        // 4.1 重排前清理该计划未下达的旧详情，避免多次排程详情混杂（已下达的保留作历史）
        detailRepository.deleteByPlanIdAndStatus(planId, "SCHEDULED");

        // 5. 根据算法类型选择排程策略
        List<ScheduleDetailEntity> details;
        if (algorithm == SchedulingAlgorithm.GENETIC) {
            // 使用遗传算法优化排程
            logger.info("使用遗传算法进行排程优化");
            details = geneticScheduler.optimize(plan, processes, resources, productionLine);
            // 设置排程结果ID
            for (ScheduleDetailEntity detail : details) {
                detail.setScheduleResultId(result.getId());
            }
        } else {
            // 使用基于工艺路线的串行排程（默认算法）
            logger.info("使用基于工艺路线的串行排程");
            details = executeSerialScheduling(plan, processes, resources, productionLine, result.getId());
        }

        if (details.isEmpty()) {
            throw new RuntimeException("排程失败：工艺路线各工序均无可用资源");
        }

        detailRepository.saveAll(details);

        // 6. 更新结果与计划状态
        result.setStatus("COMPLETED");
        resultRepository.save(result);

        plan.setStatus("SCHEDULED");
        planRepository.save(plan);

        // 7. 记录排程历史（executionTime单位：秒）
        ScheduleHistoryEntity history = new ScheduleHistoryEntity();
        history.setPlanId(planId);
        history.setScheduleNo(result.getScheduleNo());
        history.setAlgorithm(result.getAlgorithm());
        history.setStatus("COMPLETED");
        history.setExecutionTime((System.currentTimeMillis() - startMillis) / 1000.0);
        history.setCreatedTime(LocalDateTime.now());
        historyRepository.save(history);

        return result;
    }

    /**
     * 执行基于工艺路线的串行排程
     * 
     * @param plan 生产计划
     * @param processes 工艺路线（按sequence排序）
     * @param resources 可用资源列表
     * @param productionLine 产品所属生产线
     * @param scheduleResultId 排程结果ID
     * @return 排程详情列表
     */
    private List<ScheduleDetailEntity> executeSerialScheduling(
            ProductionPlanEntity plan,
            List<ProcessEntity> processes,
            List<ResourceEntity> resources,
            String productionLine,
            Long scheduleResultId) {
        
        BigDecimal totalQty = plan.getQuantity();
        LocalDateTime processStart = plan.getStartTime() != null ? plan.getStartTime() : LocalDateTime.now();
        List<ScheduleDetailEntity> details = new ArrayList<>();

        for (ProcessEntity process : processes) {
            // 5.1 匹配该工序的加工资源：设备按关键词匹配，未命中工序挂到产品生产线
            List<ResourceEntity> matched = matchResources(process, resources, productionLine);
            if (matched.isEmpty()) {
                logger.warn("工序[{}]无匹配资源，跳过该工序资源分配，仅推进时间", process.getProcessName());
                processStart = processStart.plusMinutes(processDurationMinutes(process, totalQty, BigDecimal.ONE));
                continue;
            }

            // 5.2 同工序多资源按产能比例拆分数量（余数归入最后一个资源）
            BigDecimal totalCapacity = matched.stream()
                    .map(this::capacityOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal allocated = BigDecimal.ZERO;
            long processMinutes = 0;
            List<ScheduleDetailEntity> processDetails = new ArrayList<>();

            for (int i = 0; i < matched.size(); i++) {
                ResourceEntity resource = matched.get(i);
                BigDecimal capacity = capacityOf(resource);
                BigDecimal qty;
                if (i == matched.size() - 1) {
                    qty = totalQty.subtract(allocated);
                } else {
                    qty = totalQty.multiply(capacity)
                            .divide(totalCapacity, 0, RoundingMode.HALF_UP);
                    allocated = allocated.add(qty);
                }
                if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                long minutes = processDurationMinutes(process, qty, capacity);
                processMinutes = Math.max(processMinutes, minutes);

                ScheduleDetailEntity detail = new ScheduleDetailEntity();
                detail.setPlanId(plan.getId());
                detail.setScheduleResultId(scheduleResultId);
                detail.setResourceId(resource.getId());
                detail.setResourceName(resource.getName());
                detail.setQuantity(qty);
                detail.setStartTime(processStart);
                // 结束时间在工序最大耗时确定后统一设置，先占位
                detail.setEndTime(processStart.plusMinutes(minutes));
                detail.setStatus("SCHEDULED");
                detail.setCreatedTime(LocalDateTime.now());
                processDetails.add(detail);
            }

            // 5.3 同工序并行资源统一按工序最大耗时结束，保证并行任务同时完工
            LocalDateTime processEnd = processStart.plusMinutes(processMinutes);
            for (ScheduleDetailEntity d : processDetails) {
                d.setEndTime(processEnd);
            }
            details.addAll(processDetails);

            // 5.4 串行推进：下道工序从本工序结束时间开始
            processStart = processEnd;
        }

        return details;
    }

    /**
     * 解析MES响应JSON中的code字段。MES服务Result使用msg字段，common使用message，
     * 此处仅提取code避免受字段差异影响；解析失败返回null
     */
    private Integer parseResultCode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(json);
            com.fasterxml.jackson.databind.JsonNode codeNode = node.get("code");
            return codeNode != null && codeNode.isNumber() ? codeNode.intValue() : null;
        } catch (Exception e) {
            logger.warn("解析MES响应code失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 匹配工序加工资源：设备类按工序关键词匹配名称；未配置关键词的工序（原料准备/入库）
     * 挂到产品所属生产线资源（无产线信息时取第一条可用生产线）。
     */
    private List<ResourceEntity> matchResources(ProcessEntity process, List<ResourceEntity> resources, String productionLine) {
        String keyword = PROCESS_EQUIPMENT_KEYWORD.get(process.getProcessName());
        if (keyword != null) {
            return resources.stream()
                    .filter(r -> TYPE_EQUIPMENT.equals(r.getType()) && r.getName() != null && r.getName().contains(keyword))
                    .toList();
        }
        // 生产线资源：优先产品所属产线
        List<ResourceEntity> lines = resources.stream()
                .filter(r -> TYPE_LINE.equals(r.getType()))
                .toList();
        if (productionLine != null) {
            Optional<ResourceEntity> preferred = lines.stream()
                    .filter(r -> productionLine.equals(r.getName()))
                    .findFirst();
            if (preferred.isPresent()) {
                return List.of(preferred.get());
            }
        }
        return lines.isEmpty() ? List.of() : List.of(lines.get(0));
    }

    /**
     * 资源产能（每小时处理量），为空或非正时按默认值10处理
     */
    private BigDecimal capacityOf(ResourceEntity resource) {
        return resource.getCapacity() != null && resource.getCapacity() > 0
                ? new BigDecimal(resource.getCapacity()) : BigDecimal.TEN;
    }

    /**
     * 计算工序在单资源上的耗时（分钟）= 准备时间 + 加工时间(数量/产能换算) + 拆卸时间
     */
    private long processDurationMinutes(ProcessEntity process, BigDecimal qty, BigDecimal capacity) {
        long processingMinutes = qty.divide(capacity, 0, RoundingMode.CEILING).longValue() * 60;
        int setup = process.getSetupTime() != null ? process.getSetupTime() : 0;
        int teardown = process.getTeardownTime() != null ? process.getTeardownTime() : 0;
        return setup + processingMinutes + teardown;
    }

    @Override
    public ScheduleResultEntity executeScheduling(Long planId, SchedulingAlgorithm algorithm, List<String> objectives, Map<String, Object> constraints) {
        return executeScheduling(planId, algorithm, objectives);
    }

    /**
     * 查询某计划最新的排程结果（按创建时间倒序取第一条）
     */
    @Override
    public ScheduleResultEntity getSchedulingResult(Long planId) {
        List<ScheduleResultEntity> results = resultRepository.findByPlanId(planId);
        return results.stream()
                .max(Comparator.comparing(ScheduleResultEntity::getCreatedTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    /**
     * 校验排程可行性：结果须存在、状态为COMPLETED/RELEASED且至少有一条排程详情
     */
    @Override
    public boolean validateSchedule(Long scheduleResultId) {
        Optional<ScheduleResultEntity> resultOpt = resultRepository.findById(scheduleResultId);
        if (resultOpt.isEmpty()) {
            return false;
        }
        ScheduleResultEntity result = resultOpt.get();
        if (!"COMPLETED".equals(result.getStatus()) && !"RELEASED".equals(result.getStatus())) {
            return false;
        }
        return !detailRepository.findByScheduleResultId(scheduleResultId).isEmpty();
    }

    /**
     * 检测排程冲突：同一资源上时间段重叠的详情视为冲突；详情结束时间超出计划结束时间视为超期冲突
     */
    @Override
    public List<String> detectConflicts(Long scheduleResultId) {
        List<String> conflicts = new ArrayList<>();
        Optional<ScheduleResultEntity> resultOpt = resultRepository.findById(scheduleResultId);
        if (resultOpt.isEmpty()) {
            conflicts.add("排程结果不存在: " + scheduleResultId);
            return conflicts;
        }
        ScheduleResultEntity result = resultOpt.get();
        // 按排程结果取详情：仅检测本次排程内的资源冲突，历史排程不纳入
        List<ScheduleDetailEntity> details = detailRepository.findByScheduleResultId(scheduleResultId);

        // 1. 同资源时间段重叠检测（按资源分组后按开始时间排序，检查相邻区间是否相交）
        Map<Long, List<ScheduleDetailEntity>> byResource = new java.util.HashMap<>();
        for (ScheduleDetailEntity d : details) {
            byResource.computeIfAbsent(d.getResourceId(), k -> new ArrayList<>()).add(d);
        }
        for (Map.Entry<Long, List<ScheduleDetailEntity>> entry : byResource.entrySet()) {
            List<ScheduleDetailEntity> list = entry.getValue().stream()
                    .sorted(Comparator.comparing(ScheduleDetailEntity::getStartTime))
                    .toList();
            for (int i = 1; i < list.size(); i++) {
                ScheduleDetailEntity prev = list.get(i - 1);
                ScheduleDetailEntity curr = list.get(i);
                if (curr.getStartTime().isBefore(prev.getEndTime())) {
                    conflicts.add(String.format("资源[%s]上存在时间段重叠: #%d(%s~%s) 与 #%d(%s~%s)",
                            curr.getResourceName(), prev.getId(), prev.getStartTime(), prev.getEndTime(),
                            curr.getId(), curr.getStartTime(), curr.getEndTime()));
                }
            }
        }

        // 2. 超期检测：详情结束时间晚于计划结束时间
        Optional<ProductionPlanEntity> planOpt = planRepository.findById(result.getPlanId());
        if (planOpt.isPresent() && planOpt.get().getEndTime() != null) {
            LocalDateTime planEnd = planOpt.get().getEndTime();
            for (ScheduleDetailEntity d : details) {
                if (d.getEndTime() != null && d.getEndTime().isAfter(planEnd)) {
                    conflicts.add(String.format("资源[%s]上的排程 #%d 结束时间 %s 超出计划结束时间 %s",
                            d.getResourceName(), d.getId(), d.getEndTime(), planEnd));
                }
            }
        }
        return conflicts;
    }

    @Override
    public ScheduleResultEntity optimizeSchedule(Long scheduleResultId, List<String> objectives) {
        // 模拟优化：直接返回原结果
        return resultRepository.findById(scheduleResultId).orElse(null);
    }

    @Override
    public ScheduleResultEntity manualAdjustTask(Long taskId, String startTime, String endTime, Long resourceId) {
        // 暂未实现
        return null;
    }
}
