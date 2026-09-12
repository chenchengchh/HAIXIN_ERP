package com.hxcoe.aps.service.algorithm;

import com.hxcoe.aps.entity.ProcessEntity;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.entity.ScheduleDetailEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 遗传算法排程优化器
 * 通过模拟自然选择和遗传机制寻找最优排程方案
 * 
 * 主要优化目标：
 * 1. 最小化最大完工时间（makespan）
 * 2. 平衡资源负载
 * 3. 满足工艺约束和资源约束
 */
public class GeneticAlgorithmScheduler {

    /** 种群大小 */
    private static final int POPULATION_SIZE = 50;
    
    /** 最大迭代次数 */
    private static final int MAX_GENERATIONS = 100;
    
    /** 交叉概率 */
    private static final double CROSSOVER_RATE = 0.8;
    
    /** 变异概率 */
    private static final double MUTATION_RATE = 0.1;
    
    /** 精英保留数量 */
    private static final int ELITE_SIZE = 5;
    
    private final Random random = new Random();
    
    /**
     * 染色体：表示一个完整的排程方案
     * 基因编码：每个基因代表一个工序在一台设备上的分配
     */
    private static class Chromosome {
        /** 基因序列：processIndex -> resourceIndex */
        List<Integer> genes;
        /** 适应度值（越小越好） */
        double fitness;
        /** 排程详情 */
        List<ScheduleDetailEntity> scheduleDetails;
        
        Chromosome(int geneCount) {
            this.genes = new ArrayList<>(geneCount);
            this.scheduleDetails = new ArrayList<>();
        }
        
        Chromosome(Chromosome other) {
            this.genes = new ArrayList<>(other.genes);
            this.fitness = other.fitness;
            this.scheduleDetails = new ArrayList<>();
            for (ScheduleDetailEntity detail : other.scheduleDetails) {
                this.scheduleDetails.add(copyDetail(detail));
            }
        }
    }
    
    /**
     * 使用遗传算法优化排程
     * 
     * @param plan 生产计划
     * @param processes 工艺路线（按sequence排序）
     * @param resources 可用资源列表
     * @param productionLine 产品所属生产线
     * @return 优化后的排程详情列表
     */
    public List<ScheduleDetailEntity> optimize(
            ProductionPlanEntity plan,
            List<ProcessEntity> processes,
            List<ResourceEntity> resources,
            String productionLine) {
        
        int geneCount = processes.size();
        if (geneCount == 0) {
            return new ArrayList<>();
        }
        
        // 1. 初始化种群
        List<Chromosome> population = initializePopulation(geneCount, resources.size());
        
        // 2. 评估初始种群
        for (Chromosome chromosome : population) {
            evaluateChromosome(chromosome, plan, processes, resources, productionLine);
        }
        
        // 3. 进化迭代
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // 3.1 选择
            List<Chromosome> parents = selection(population);
            
            // 3.2 交叉和变异生成新种群
            List<Chromosome> offspring = new ArrayList<>();
            
            // 保留精英
            population.sort(Comparator.comparingDouble(c -> c.fitness));
            for (int i = 0; i < ELITE_SIZE && i < population.size(); i++) {
                offspring.add(new Chromosome(population.get(i)));
            }
            
            // 生成剩余个体
            while (offspring.size() < POPULATION_SIZE) {
                Chromosome parent1 = parents.get(random.nextInt(parents.size()));
                Chromosome parent2 = parents.get(random.nextInt(parents.size()));
                
                Chromosome child;
                if (random.nextDouble() < CROSSOVER_RATE) {
                    child = crossover(parent1, parent2);
                } else {
                    child = new Chromosome(parent1);
                }
                
                if (random.nextDouble() < MUTATION_RATE) {
                    mutate(child, resources.size());
                }
                
                offspring.add(child);
            }
            
            // 3.3 评估新种群
            for (Chromosome chromosome : offspring) {
                if (chromosome.scheduleDetails.isEmpty()) {
                    evaluateChromosome(chromosome, plan, processes, resources, productionLine);
                }
            }
            
            population = offspring;
        }
        
        // 4. 返回最优个体的排程方案
        population.sort(Comparator.comparingDouble(c -> c.fitness));
        return population.get(0).scheduleDetails;
    }
    
    /**
     * 初始化种群
     */
    private List<Chromosome> initializePopulation(int geneCount, int resourceCount) {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome chromosome = new Chromosome(geneCount);
            for (int j = 0; j < geneCount; j++) {
                chromosome.genes.add(random.nextInt(resourceCount));
            }
            population.add(chromosome);
        }
        return population;
    }
    
    /**
     * 评估染色体适应度
     * 适应度函数：综合考虑完工时间和资源负载均衡
     */
    private void evaluateChromosome(
            Chromosome chromosome,
            ProductionPlanEntity plan,
            List<ProcessEntity> processes,
            List<ResourceEntity> resources,
            String productionLine) {
        
        chromosome.scheduleDetails.clear();
        BigDecimal totalQty = plan.getQuantity();
        LocalDateTime processStart = plan.getStartTime() != null ? plan.getStartTime() : LocalDateTime.now();
        
        // 记录每个资源的工作时间，用于计算负载均衡
        Map<Long, Long> resourceWorkTime = new java.util.HashMap<>();
        
        // 按工艺路线串行排程
        for (int i = 0; i < processes.size(); i++) {
            ProcessEntity process = processes.get(i);
            int resourceIndex = chromosome.genes.get(i);
            
            // 匹配该工序的资源
            List<ResourceEntity> matched = matchResources(process, resources, productionLine, resourceIndex);
            if (matched.isEmpty()) {
                // 无匹配资源，仅推进时间
                processStart = processStart.plusMinutes(processDurationMinutes(process, totalQty, BigDecimal.ONE));
                continue;
            }
            
            // 同工序多资源按产能比例拆分数量
            BigDecimal totalCapacity = matched.stream()
                    .map(this::capacityOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal allocated = BigDecimal.ZERO;
            long processMinutes = 0;
            List<ScheduleDetailEntity> processDetails = new ArrayList<>();
            
            for (int j = 0; j < matched.size(); j++) {
                ResourceEntity resource = matched.get(j);
                BigDecimal capacity = capacityOf(resource);
                BigDecimal qty;
                if (j == matched.size() - 1) {
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
                
                // 累加资源工作时间
                resourceWorkTime.merge(resource.getId(), minutes, Long::sum);
                
                ScheduleDetailEntity detail = new ScheduleDetailEntity();
                detail.setPlanId(plan.getId());
                detail.setResourceId(resource.getId());
                detail.setResourceName(resource.getName());
                detail.setQuantity(qty);
                detail.setStartTime(processStart);
                detail.setEndTime(processStart.plusMinutes(minutes));
                detail.setStatus("SCHEDULED");
                detail.setCreatedTime(LocalDateTime.now());
                processDetails.add(detail);
            }
            
            // 同工序并行资源统一按工序最大耗时结束
            LocalDateTime processEnd = processStart.plusMinutes(processMinutes);
            for (ScheduleDetailEntity d : processDetails) {
                d.setEndTime(processEnd);
            }
            chromosome.scheduleDetails.addAll(processDetails);
            
            // 串行推进：下道工序从本工序结束时间开始
            processStart = processEnd;
        }
        
        // 计算适应度：完工时间 + 资源负载不均衡度
        long makespan = calculateMakespan(chromosome.scheduleDetails);
        double loadImbalance = calculateLoadImbalance(resourceWorkTime);
        
        // 适应度 = 完工时间（分钟）+ 负载不均衡惩罚
        chromosome.fitness = makespan + loadImbalance * 100;
    }
    
    /**
     * 计算最大完工时间（分钟）
     */
    private long calculateMakespan(List<ScheduleDetailEntity> details) {
        if (details.isEmpty()) {
            return 0;
        }
        LocalDateTime minStart = details.stream()
                .map(ScheduleDetailEntity::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        LocalDateTime maxEnd = details.stream()
                .map(ScheduleDetailEntity::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        return java.time.Duration.between(minStart, maxEnd).toMinutes();
    }
    
    /**
     * 计算资源负载不均衡度（标准差/平均值）
     */
    private double calculateLoadImbalance(Map<Long, Long> resourceWorkTime) {
        if (resourceWorkTime.isEmpty()) {
            return 0;
        }
        
        double mean = resourceWorkTime.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        
        if (mean == 0) {
            return 0;
        }
        
        double variance = resourceWorkTime.values().stream()
                .mapToDouble(time -> Math.pow(time - mean, 2))
                .average()
                .orElse(0);
        
        return Math.sqrt(variance) / mean;
    }
    
    /**
     * 选择操作：锦标赛选择
     */
    private List<Chromosome> selection(List<Chromosome> population) {
        List<Chromosome> parents = new ArrayList<>();
        int tournamentSize = 3;
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome best = null;
            for (int j = 0; j < tournamentSize; j++) {
                Chromosome candidate = population.get(random.nextInt(population.size()));
                if (best == null || candidate.fitness < best.fitness) {
                    best = candidate;
                }
            }
            parents.add(best);
        }
        
        return parents;
    }
    
    /**
     * 交叉操作：单点交叉
     */
    private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        int geneCount = parent1.genes.size();
        if (geneCount <= 1) {
            return new Chromosome(parent1);
        }
        
        int crossoverPoint = random.nextInt(geneCount - 1) + 1;
        Chromosome child = new Chromosome(geneCount);
        
        for (int i = 0; i < geneCount; i++) {
            if (i < crossoverPoint) {
                child.genes.add(parent1.genes.get(i));
            } else {
                child.genes.add(parent2.genes.get(i));
            }
        }
        
        return child;
    }
    
    /**
     * 变异操作：随机改变某个基因
     */
    private void mutate(Chromosome chromosome, int resourceCount) {
        int geneCount = chromosome.genes.size();
        if (geneCount == 0) {
            return;
        }
        
        int mutationPoint = random.nextInt(geneCount);
        chromosome.genes.set(mutationPoint, random.nextInt(resourceCount));
    }
    
    /**
     * 匹配工序加工资源：根据基因选择起始资源索引
     */
    private List<ResourceEntity> matchResources(
            ProcessEntity process,
            List<ResourceEntity> resources,
            String productionLine,
            int startIndex) {
        
        // 设备类工序按关键词匹配
        String keyword = getProcessEquipmentKeyword(process.getProcessName());
        if (keyword != null) {
            List<ResourceEntity> matched = resources.stream()
                    .filter(r -> "设备".equals(r.getType()) && r.getName() != null && r.getName().contains(keyword))
                    .collect(Collectors.toList());
            
            // 根据基因索引选择资源子集（模拟不同分配方案）
            if (!matched.isEmpty() && startIndex < matched.size()) {
                // 从startIndex开始选择最多2台设备
                int endIndex = Math.min(startIndex + 2, matched.size());
                return matched.subList(startIndex, endIndex);
            }
            return matched;
        }
        
        // 生产线资源
        List<ResourceEntity> lines = resources.stream()
                .filter(r -> "生产线".equals(r.getType()))
                .collect(Collectors.toList());
        
        if (productionLine != null) {
            return lines.stream()
                    .filter(r -> productionLine.equals(r.getName()))
                    .findFirst()
                    .map(List::of)
                    .orElse(lines.isEmpty() ? List.of() : List.of(lines.get(0)));
        }
        
        return lines.isEmpty() ? List.of() : List.of(lines.get(0));
    }
    
    /**
     * 获取工序设备关键词
     */
    private String getProcessEquipmentKeyword(String processName) {
        Map<String, String> keywordMap = Map.of(
                "乳化", "乳化",
                "均质", "乳化",
                "冷却", "乳化",
                "灌装", "灌装",
                "贴标", "贴标",
                "包装", "包装",
                "QC检验", "检验");
        return keywordMap.get(processName);
    }
    
    /**
     * 资源产能（每小时处理量）
     */
    private BigDecimal capacityOf(ResourceEntity resource) {
        return resource.getCapacity() != null && resource.getCapacity() > 0
                ? new BigDecimal(resource.getCapacity()) : BigDecimal.TEN;
    }
    
    /**
     * 计算工序在单资源上的耗时（分钟）
     */
    private long processDurationMinutes(ProcessEntity process, BigDecimal qty, BigDecimal capacity) {
        long processingMinutes = qty.divide(capacity, 0, RoundingMode.CEILING).longValue() * 60;
        int setup = process.getSetupTime() != null ? process.getSetupTime() : 0;
        int teardown = process.getTeardownTime() != null ? process.getTeardownTime() : 0;
        return setup + processingMinutes + teardown;
    }
    
    /**
     * 复制排程详情
     */
    private static ScheduleDetailEntity copyDetail(ScheduleDetailEntity source) {
        ScheduleDetailEntity target = new ScheduleDetailEntity();
        target.setId(source.getId());
        target.setPlanId(source.getPlanId());
        target.setScheduleResultId(source.getScheduleResultId());
        target.setResourceId(source.getResourceId());
        target.setResourceName(source.getResourceName());
        target.setQuantity(source.getQuantity());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setStatus(source.getStatus());
        target.setCreatedTime(source.getCreatedTime());
        return target;
    }
}
