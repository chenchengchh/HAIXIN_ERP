package com.hxcoe.mes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.mes.service.WorkOrderService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * MES 工单创建事件 Redis Stream 消费 Job（REDIS_STREAM 模式消费端）。
 *
 * <p>复刻 ERP 的 {@code ErpPoFactRedisStreamJob}：定时轮询消费组，
 * 反序列化事件后调用 {@link WorkOrderService#createWorkOrderFromErp} 创建工单。
 *
 * <p>仅在 hxcoe.integration.transport=REDIS_STREAM 时启用。
 * 读取失败时采用指数退避策略，避免 Redis 不可用时压满 CPU。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "REDIS_STREAM")
public class WorkOrderCreateRedisStreamJob {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkOrderService workOrderService;

    /** 下次允许读取的时间戳（退避用） */
    private volatile long nextAllowedAtMs = 0L;

    /** 连续读取失败次数（用于指数退避） */
    private int consecutiveReadFailures = 0;

    /**
     * 定时轮询消费 ERP 推送的工单创建事件。
     */
    @Scheduled(fixedDelayString = "${hxcoe.integration.redis.poll-delay-ms:1000}")
    public void poll() {
        if (stringRedisTemplate == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextAllowedAtMs) {
            return;
        }
        String stream = integrationTransportProperties.getRedis().getStreamMesWorkOrderCreate();
        String group = integrationTransportProperties.getRedis().getGroupMesWorkOrderCreate();
        String consumer = integrationTransportProperties.getRedis().getConsumerMesWorkOrderCreate();

        ensureGroup(stream, group);

        List<MapRecord<String, Object, Object>> records;
        try {
            records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(group, consumer),
                    StreamReadOptions.empty().count(20).block(Duration.ofMillis(200)),
                    StreamOffset.create(stream, ReadOffset.lastConsumed())
            );
        } catch (Exception ex) {
            consecutiveReadFailures = Math.min(consecutiveReadFailures + 1, 10);
            long delayMs = Math.min(60_000L, 1000L * (1L << Math.min(consecutiveReadFailures, 6)));
            nextAllowedAtMs = System.currentTimeMillis() + delayMs;
            log.warn("MES Redis Stream 读取失败 stream={} error={} nextRetryInMs={}", stream, ex.getMessage(), delayMs);
            return;
        }
        consecutiveReadFailures = 0;
        nextAllowedAtMs = 0L;
        if (records == null || records.isEmpty()) {
            return;
        }
        for (MapRecord<String, Object, Object> r : records) {
            try {
                Map<Object, Object> v = r.getValue();
                Object bodyObj = v == null ? null : v.get("body");
                String body = bodyObj == null ? null : String.valueOf(bodyObj);
                if (body == null || body.isBlank()) {
                    ack(stream, group, r);
                    continue;
                }
                WorkOrderCreateRequestDTO req = objectMapper.readValue(body, WorkOrderCreateRequestDTO.class);
                if (req != null && req.getWorkOrder() != null) {
                    workOrderService.createWorkOrderFromErp(req);
                }
                ack(stream, group, r);
            } catch (Exception ex) {
                log.warn("MES Redis Stream 消费失败 stream={} recordId={} error={}", stream, r.getId(), ex.getMessage());
            }
        }
    }

    /**
     * 确保消费组存在（幂等创建，MKSTREAM 自动建流）。
     */
    private void ensureGroup(String stream, String group) {
        try {
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
                createGroupMkStream(connection, stream, group);
                return null;
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * 创建消费组（流不存在时自动创建）。
     */
    private void createGroupMkStream(RedisConnection connection, String stream, String group) {
        byte[] key = stringRedisTemplate.getStringSerializer().serialize(stream);
        if (key == null) {
            return;
        }
        try {
            connection.xGroupCreate(key, group, ReadOffset.from("0-0"), true);
        } catch (Exception ignored) {
        }
    }

    /**
     * ACK 确认消息（失败时忽略，由 PEL 兜底重投）。
     */
    private void ack(String stream, String group, MapRecord<String, Object, Object> record) {
        try {
            stringRedisTemplate.opsForStream().acknowledge(stream, group, record.getId());
        } catch (Exception ignored) {
        }
    }
}
