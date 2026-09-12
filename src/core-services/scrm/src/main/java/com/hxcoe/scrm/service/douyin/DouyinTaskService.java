package com.hxcoe.scrm.service.douyin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scrm.dto.douyin.DouyinTaskDTO;
import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.entity.douyin.DouyinTaskEntity;
import com.hxcoe.scrm.entity.douyin.DouyinVideoEntity;
import com.hxcoe.scrm.entity.douyin.TaskLogEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.repository.douyin.DouyinCustomerRepository;
import com.hxcoe.scrm.repository.douyin.DouyinTaskRepository;
import com.hxcoe.scrm.repository.douyin.DouyinVideoRepository;
import com.hxcoe.scrm.repository.douyin.TaskLogRepository;
import com.hxcoe.scrm.service.CustomerService;
import com.hxcoe.scrm.entity.CustomerEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DouyinTaskService {
    private static final Logger logger = LoggerFactory.getLogger(DouyinTaskService.class);

    @Autowired
    private DouyinTaskRepository taskRepository;

    @Autowired
    private TaskLogRepository logRepository;

    @Autowired
    private DouyinVideoRepository videoRepository;

    @Autowired
    private DouyinCustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DouyinCrawlService crawlService;

    @Autowired
    private com.hxcoe.scrm.service.douyin.DouyinMessageService messageService;

    @Autowired
    private DouyinModuleGuard moduleGuard;

    @Autowired
    private com.hxcoe.scrm.service.douyin.DouyinStatsService statsService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 任务执行线程池
    private volatile ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Boolean> runningTasks = new ConcurrentHashMap<>();

    private ScheduledExecutorService getOrCreateScheduler() {
        if (scheduler != null && !scheduler.isShutdown() && !scheduler.isTerminated()) {
            return scheduler;
        }
        synchronized (this) {
            if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
                scheduler = Executors.newScheduledThreadPool(2);
            }
            return scheduler;
        }
    }

    private void scheduleExecute(Long taskId, long delaySeconds) {
        ScheduledExecutorService exec = getOrCreateScheduler();
        ScheduledFuture<?> future = exec.schedule(() -> executeTask(taskId), delaySeconds, TimeUnit.SECONDS);
        ScheduledFuture<?> old = scheduledFutures.put(taskId, future);
        if (old != null) {
            old.cancel(false);
        }
    }

    private void shutdownSchedulerIfIdle() {
        if (!runningTasks.isEmpty() || !scheduledFutures.isEmpty()) {
            return;
        }
        ScheduledExecutorService exec = scheduler;
        if (exec == null) {
            return;
        }
        synchronized (this) {
            if (!runningTasks.isEmpty() || !scheduledFutures.isEmpty()) {
                return;
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
                scheduler = null;
            }
        }
    }

    public List<DouyinTaskDTO> getAllTasks() {
        List<DouyinTaskEntity> entities = taskRepository.findAll();
        return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public DouyinTaskEntity createTask(DouyinTaskEntity task) {
        // 数据验证
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (task.getSearchKeyword() == null || task.getSearchKeyword().trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        // 设置默认状态
        task.setStatus("stopped");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        // 确保 JSON 字段不为 null，设置合理默认值
        if (task.getIntentKeywords() == null) {
            try {
                task.setIntentKeywords(objectMapper.writeValueAsString(List.of("多少钱", "怎么买", "求链接", "感兴趣", "价格")));
            } catch (Exception e) {
                task.setIntentKeywords("[]");
            }
        }
        if (task.getExcludeKeywords() == null) {
            try {
                task.setExcludeKeywords(objectMapper.writeValueAsString(List.of("互粉", "骗子")));
            } catch (Exception e) {
                task.setExcludeKeywords("[]");
            }
        }
        
        // 设置默认消息模板
        if (task.getMessageTemplate() == null || task.getMessageTemplate().trim().isEmpty()) {
            task.setMessageTemplate("你好，刚看到你在评论区问关于{产品}的问题，我是厂家直销，可以加个V详细发您资料参考下~");
        }
        
        // 设置默认最大发送数量
        if (task.getMaxMessageCount() == null) {
            task.setMaxMessageCount(10);
        }
        
        // 设置默认发送间隔
        if (task.getMessageInterval() == null) {
            try {
                task.setMessageInterval(objectMapper.writeValueAsString(new Integer[]{5, 15}));
            } catch (Exception e) {
                task.setMessageInterval("[5,15]");
            }
        }
        
        return taskRepository.save(task);
    }
    
    // 仅更新任务配置
    public DouyinTaskEntity updateTask(Long id, DouyinTaskEntity taskData) {
        DouyinTaskEntity task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在，id: " + id));
        
        // 数据验证和属性更新
        if (taskData.getName() != null) {
            if (taskData.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("任务名称不能为空");
            }
            task.setName(taskData.getName().trim());
        }
        
        if (taskData.getSearchKeyword() != null) {
            if (taskData.getSearchKeyword().trim().isEmpty()) {
                throw new IllegalArgumentException("搜索关键词不能为空");
            }
            task.setSearchKeyword(taskData.getSearchKeyword().trim());
        }
        
        // 更新关键词列表，只更新非null值
        if (taskData.getIntentKeywords() != null) {
            task.setIntentKeywords(taskData.getIntentKeywords());
        }
        if (taskData.getExcludeKeywords() != null) {
            task.setExcludeKeywords(taskData.getExcludeKeywords());
        }
        
        // 更新消息配置，只更新非null值
        if (taskData.getMessageTemplate() != null) {
            task.setMessageTemplate(taskData.getMessageTemplate());
        }
        
        if (taskData.getMaxMessageCount() != null) {
            if (taskData.getMaxMessageCount() < 1 || taskData.getMaxMessageCount() > 100) {
                throw new IllegalArgumentException("发送数量必须在1-100之间");
            }
            task.setMaxMessageCount(taskData.getMaxMessageCount());
        }
        
        if (taskData.getMessageInterval() != null) {
            task.setMessageInterval(taskData.getMessageInterval());
        }
        
        // 更新时间戳
        task.setUpdateTime(LocalDateTime.now());
        
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        stopTask(id);
        taskRepository.deleteById(id);
        // 关联删除视频、客户、日志等逻辑根据需求添加，这里暂略
    }

    public List<DouyinVideoEntity> getTaskVideos(Long taskId) {
        return videoRepository.findByTaskId(taskId);
    }

    public List<DouyinCustomerEntity> getTaskCustomers(Long taskId) {
        return customerRepository.findByTaskId(taskId);
    }

    public void convertCustomer(Long id) {
        DouyinCustomerEntity douyinCustomer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Douyin Customer not found"));
        
        if ("converted".equals(douyinCustomer.getStatus())) {
            throw new RuntimeException("Customer already converted");
        }

        // 创建客户实体
        CustomerEntity customer = CustomerEntity.builder()
            .name(douyinCustomer.getNickname())
            .source("Douyin")
            .status("potential")
            .level("level1") // 初始等级
            .customerCode(generateCustomerCode())
            .remark(buildCustomerRemark(douyinCustomer))
            .createdBy("system")
            .build();
        
        // 保存客户
        CustomerEntity createdCustomer = customerService.createCustomer(customer);
        
        // 更新抖音客户状态为已转化
        douyinCustomer.setStatus("converted");
        customerRepository.save(douyinCustomer);
        
        // 添加转化日志
        addLog(douyinCustomer.getTaskId(), "success", "客户转化成功: " + douyinCustomer.getNickname() + " -> 正式客户ID: " + createdCustomer.getId());
    }

    /**
     * 生成客户编号
     */
    private String generateCustomerCode() {
        // 格式：DY + 年份后两位 + 月份 + 日 + 6位随机数
        LocalDateTime now = LocalDateTime.now();
        String dateStr = String.format("%02d%02d%02d", 
            now.getYear() % 100, 
            now.getMonthValue(), 
            now.getDayOfMonth());
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return "DY" + dateStr + randomStr;
    }

    /**
     * 构建客户备注信息
     */
    private String buildCustomerRemark(DouyinCustomerEntity douyinCustomer) {
        StringBuilder remark = new StringBuilder();
        remark.append("来源: 抖音主动获客\n");
        remark.append("任务ID: " + douyinCustomer.getTaskId() + "\n");
        remark.append("昵称: " + douyinCustomer.getNickname() + "\n");
        remark.append("评论内容: " + douyinCustomer.getCommentContent() + "\n");
        remark.append("命中关键词: " + douyinCustomer.getMatchKeyword() + "\n");
        remark.append("转化时间: " + LocalDateTime.now());
        return remark.toString();
    }

    public void updateCustomerStatus(Long id, String status) {
        DouyinCustomerEntity customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setStatus(status);
        customerRepository.save(customer);
    }

    public void startTask(Long id) {
        moduleGuard.requireRedisAvailable("启动抖音任务");
        moduleGuard.requireBrowserAvailable("启动抖音任务");
        DouyinTaskEntity task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("running");
        task.setLastRunTime(LocalDateTime.now());
        taskRepository.save(task);
        
        runningTasks.put(id, true);
        
        // 启动真实执行逻辑
        addLog(id, "info", "任务启动，正在初始化抖音采集引擎...");
        scheduleExecute(id, 2);
    }

    public void stopTask(Long id) {
        // 如果任务不存在，忽略
        taskRepository.findById(id).ifPresent(task -> {
            task.setStatus("stopped");
            taskRepository.save(task);
        });
        runningTasks.remove(id);
        ScheduledFuture<?> future = scheduledFutures.remove(id);
        if (future != null) {
            future.cancel(false);
        }
        shutdownSchedulerIfIdle();
        addLog(id, "warning", "任务已手动停止");
    }

    private DouyinTaskDTO convertToDTO(DouyinTaskEntity entity) {
        DouyinTaskDTO dto = new DouyinTaskDTO();
        BeanUtils.copyProperties(entity, dto);

        // Filter Config
        DouyinTaskDTO.FilterConfigDTO filter = new DouyinTaskDTO.FilterConfigDTO();
        try {
            // 安全解析意向关键词
            filter.setIntentKeywords(objectMapper.readValue(
                entity.getIntentKeywords() != null ? entity.getIntentKeywords() : "[]", 
                new TypeReference<List<String>>() {}
            ));
            // 安全解析排除关键词
            filter.setExcludeKeywords(objectMapper.readValue(
                entity.getExcludeKeywords() != null ? entity.getExcludeKeywords() : "[]", 
                new TypeReference<List<String>>() {}
            ));
        } catch (Exception e) {
            // 异常情况下使用默认值，确保数据完整性
            filter.setIntentKeywords(new ArrayList<>(List.of("多少钱", "怎么买", "求链接", "感兴趣", "价格")));
            filter.setExcludeKeywords(new ArrayList<>(List.of("互粉", "骗子")));
        }
        dto.setFilterConfig(filter);

        // Message Config
        DouyinTaskDTO.MessageConfigDTO msgConfig = new DouyinTaskDTO.MessageConfigDTO();
        // 设置消息模板，使用默认值兜底
        msgConfig.setTemplate(entity.getMessageTemplate() != null ? entity.getMessageTemplate() : 
            "你好，刚看到你在评论区问关于{产品}的问题，我是厂家直销，可以加个V详细发您资料参考下~");
        // 设置最大发送数量，使用默认值兜底
        msgConfig.setMaxCount(entity.getMaxMessageCount() != null ? entity.getMaxMessageCount() : 10);
        
        // 解析发送间隔，处理新增的messageInterval字段
        Integer[] defaultInterval = {5, 15};
        try {
            if (entity.getMessageInterval() != null) {
                Integer[] interval = objectMapper.readValue(
                    entity.getMessageInterval(), 
                    Integer[].class
                );
                // 验证间隔数组格式，确保是两个元素
                if (interval != null && interval.length == 2) {
                    msgConfig.setInterval(interval);
                } else {
                    msgConfig.setInterval(defaultInterval);
                }
            } else {
                // 如果没有设置，使用默认值
                msgConfig.setInterval(defaultInterval);
            }
        } catch (Exception e) {
            // 解析失败时使用默认值
            msgConfig.setInterval(defaultInterval);
        }
        dto.setMessageConfig(msgConfig);

        // Stats
        DouyinTaskDTO.TaskStatsDTO stats = new DouyinTaskDTO.TaskStatsDTO();
        stats.setVideosFound(videoRepository.countByTaskId(entity.getId()));
        stats.setCustomersFound(customerRepository.countByTaskId(entity.getId()));
        stats.setMessagesSent(customerRepository.countByTaskIdAndStatus(entity.getId(), "sent"));
        dto.setStats(stats);

        // Logs - 安全获取日志列表
        List<TaskLogEntity> logs = logRepository.findByTaskIdOrderByCreateTimeAsc(entity.getId());
        dto.setLogs(logs != null ? logs : new ArrayList<>());

        return dto;
    }

    private void executeTask(Long taskId) {
        if (!runningTasks.containsKey(taskId)) return;

        DouyinTaskEntity task = taskRepository.findById(taskId).orElse(null);
        if (task == null || !"running".equals(task.getStatus())) {
            runningTasks.remove(taskId);
            return;
        }

        try {
            // 获取任务配置
            List<String> intentKeywords = new ArrayList<>();
            List<String> excludeKeywords = new ArrayList<>();
            
            try {
                if (task.getIntentKeywords() != null) {
                    intentKeywords = objectMapper.readValue(task.getIntentKeywords(), new TypeReference<List<String>>(){});
                }
                if (task.getExcludeKeywords() != null) {
                    excludeKeywords = objectMapper.readValue(task.getExcludeKeywords(), new TypeReference<List<String>>(){});
                }
            } catch (Exception e) {
                addLog(taskId, "warning", "解析关键词配置失败: " + e.getMessage());
                intentKeywords = List.of("多少钱", "怎么联系", "私信", "感兴趣", "求教程");
                excludeKeywords = List.of("广告", "骗子", "垃圾");
            }

            // 步骤1: 搜索视频
            addLog(taskId, "info", "开始搜索关键词: " + task.getSearchKeyword());
            List<DouyinVideoEntity> videos = crawlService.searchVideos(task.getSearchKeyword(), taskId);
            
            // 保存视频信息
            for (DouyinVideoEntity video : videos) {
                videoRepository.save(video);
                addLog(taskId, "info", "发现新视频: " + video.getTitle());
            }

            // 步骤2: 获取视频评论并识别意向客户
            if (!videos.isEmpty()) {
                addLog(taskId, "info", "开始获取视频评论");
                for (DouyinVideoEntity video : videos) {
                    try {
                        List<DouyinCustomerEntity> customers = crawlService.getVideoComments(video.getVideoUrl(), taskId);
                        
                        // 保存意向客户信息
                        for (DouyinCustomerEntity customer : customers) {
                            customerRepository.save(customer);
                            addLog(taskId, "success", "采集到意向客户: " + customer.getNickname() + "，命中词: " + customer.getMatchKeyword());
                        }
                    } catch (DouyinException e) {
                        if (e.getStatusCode() == 503) {
                            throw e;
                        }
                        addLog(taskId, "error", "获取视频评论失败 (" + video.getTitle() + "): " + e.getMessage());
                    } catch (Exception e) {
                        addLog(taskId, "error", "获取视频评论失败 (" + video.getTitle() + "): " + e.getMessage());
                    }
                }
            }

            // 步骤3: 发送私信给意向客户
            addLog(taskId, "info", "开始处理私信发送");
            processPendingCustomers(task);

            // 步骤4: 收集客户数据用于统计
            List<DouyinCustomerEntity> customers = customerRepository.findByTaskId(taskId);
            
            // 更新统计数据
            int videosFound = videos.size();
            int customersFound = customers.size();
            // 这里简化处理，实际应统计真正发送成功的数量
            int messagesSent = customersFound;
            int convertedCustomers = 0; // 暂时设为0，实际应从数据库查询
            int failedCustomers = 0; // 暂时设为0，实际应从数据库查询
            
            try {
                statsService.updateStats(taskId, videosFound, customersFound, messagesSent, convertedCustomers, failedCustomers);
                addLog(taskId, "info", "统计数据更新成功");
            } catch (Exception e) {
                addLog(taskId, "warning", "更新统计数据失败: " + e.getMessage());
            }

        } catch (DouyinException e) {
            if (e.getStatusCode() == 503) {
                markTaskDegraded(task, e.getMessage());
                return;
            }
            addLog(taskId, "error", "任务执行异常: " + e.getMessage(), e);
        } catch (Exception e) {
            addLog(taskId, "error", "任务执行异常: " + e.getMessage(), e);
        }
        
        // 继续下一次执行，间隔 60-120 秒
        if (runningTasks.containsKey(taskId)) {
            scheduleExecute(taskId, 60 + new Random().nextInt(60));
        } else {
            scheduledFutures.remove(taskId);
            shutdownSchedulerIfIdle();
        }
    }

    /**
     * 处理待发送私信的客户
     */
    private void processPendingCustomers(DouyinTaskEntity task) {
        try {
            // 获取待发送私信的客户
            List<DouyinCustomerEntity> pendingCustomers = customerRepository.findByTaskIdAndStatus(task.getId(), "pending");
            
            if (pendingCustomers.isEmpty()) {
                addLog(task.getId(), "info", "没有待发送私信的客户");
                return;
            }

            // 获取私信模板，默认模板
            String messageTemplate = task.getMessageTemplate();
            if (messageTemplate == null || messageTemplate.isEmpty()) {
                messageTemplate = "您好{nickname}，看到您对{keyword}感兴趣，我们可以详细交流一下哦！";
            }

            // 限制发送数量
            int maxSendCount = task.getMaxMessageCount() != null ? task.getMaxMessageCount() : 10;
            
            // 批量发送私信
            addLog(task.getId(), "info", "开始发送私信，待发送客户数: " + pendingCustomers.size() + ", 最大发送数: " + maxSendCount);
            
            int sentCount = messageService.sendBatchPrivateMessages(pendingCustomers, messageTemplate, maxSendCount);
            
            addLog(task.getId(), "info", "私信发送完成，成功发送 " + sentCount + " 条，失败 " + (pendingCustomers.size() - sentCount) + " 条");
        } catch (DouyinException e) {
            if (e.getStatusCode() == 503) {
                throw e;
            }
            addLog(task.getId(), "error", "处理待发送客户失败: " + e.getMessage(), e);
        } catch (Exception e) {
            addLog(task.getId(), "error", "处理待发送客户失败: " + e.getMessage(), e);
        }
    }

    private void markTaskDegraded(DouyinTaskEntity task, String message) {
        task.setStatus("degraded");
        task.setUpdateTime(LocalDateTime.now());
        taskRepository.save(task);
        runningTasks.remove(task.getId());
        scheduledFutures.remove(task.getId());
        shutdownSchedulerIfIdle();
        addLog(task.getId(), "warning", "任务进入显式降级模式: " + message);
    }

    /**
     * 添加带异常栈的日志
     */
    private void addLog(Long taskId, String type, String message, Throwable e) {
        TaskLogEntity log = new TaskLogEntity();
        log.setTaskId(taskId);
        log.setLogType(type);
        log.setMessage(message + "\n" + getStackTraceAsString(e));
        log.setCreateTime(LocalDateTime.now());
        logRepository.save(log);
    }

    /**
     * 将异常栈转换为字符串
     */
    private String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private void addLog(Long taskId, String type, String message) {
        TaskLogEntity log = new TaskLogEntity();
        log.setTaskId(taskId);
        log.setLogType(type);
        log.setMessage(message);
        log.setCreateTime(LocalDateTime.now());
        logRepository.save(log);
    }
}
