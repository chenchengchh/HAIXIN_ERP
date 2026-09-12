package com.hxcoe.scm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.scm.service.MesCompletionFactService;
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
 * SCM 消费 MES 工单完工事件 Redis Stream Job（B6 闭环，REDIS_STREAM 模式消费端）。
 *
 * <p>仅在 hxcoe.integration.transport=REDIS_STREAM 时启用。
 * 消费流 hxcoe:integration:scm.mes-work-order-completion.v1（SCM 侧消费组）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "REDIS_STREAM")
public class MesCompletionRedisStreamJob {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MesCompletionFactService mesCompletionFactService;

    /** 下次允许读取的时间戳（退避用） */
    private volatile long nextAllowedAtMs = 0L;

    /** 连续读取失败次数（用于指数退避） */
    private int consecutiveReadFailures = 0;

    /**
     * 定时轮询消费 MES 推送的完工事件。
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
        String stream = integrationTransportProperties.getRedis().getStreamMesWorkOrderCompletionScm();
        String group = integrationTransportProperties.getRedis().getGroupMesWorkOrderCompletionScm();
        String consumer = integrationTransportProperties.getRedis().getConsumerMesWorkOrderCompletionScm();

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
            log.warn("SCM Redis Stream 读取完工事件失败 stream={} error={} nextRetryInMs={}", stream, ex.getMessage(), delayMs);
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
                MesCompletionFactDTO req = objectMapper.readValue(body, MesCompletionFactDTO.class);
                if (req != null && req.getErpProductionNo() != null) {
                    mesCompletionFactService.applyMesCompletion(req);
                }
                ack(stream, group, r);
            } catch (Exception ex) {
                log.warn("SCM Redis Stream 消费完工事件失败 stream={} recordId={} error={}", stream, r.getId(), ex.getMessage());
            }
        }
    }

    /**
     * 确保消费组存在（幂等创建）。
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
     * ACK 确认消息。
     */
    private void ack(String stream, String group, MapRecord<String, Object, Object> record) {
        try {
            stringRedisTemplate.opsForStream().acknowledge(stream, group, record.getId());
        } catch (Exception ignored) {
        }
    }
}
