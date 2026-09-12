package com.hxcoe.srm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.srm.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.srm.service.PurchaseOrderMirrorService;
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

@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "REDIS_STREAM")
public class ScmPoEventRedisStreamJob {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseOrderMirrorService purchaseOrderMirrorService;

    private volatile long nextAllowedAtMs = 0L;
    private int consecutiveReadFailures = 0;

    @Scheduled(fixedDelayString = "${hxcoe.integration.redis.poll-delay-ms:1000}")
    public void poll() {
        if (stringRedisTemplate == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextAllowedAtMs) {
            return;
        }
        String stream = integrationTransportProperties.getRedis().getStreamScmPoEvents();
        String group = integrationTransportProperties.getRedis().getGroupScmPoEvents();
        String consumer = integrationTransportProperties.getRedis().getConsumerScmPoEvents();

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
            log.warn("SRM Redis Stream 读取失败 stream={} error={} nextRetryInMs={}", stream, ex.getMessage(), delayMs);
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
                PurchaseOrderEventRequest req = objectMapper.readValue(body, PurchaseOrderEventRequest.class);
                purchaseOrderMirrorService.applyScmEvent(req);
                ack(stream, group, r);
            } catch (Exception ex) {
                log.warn("SRM Redis Stream 消费失败 stream={} recordId={} error={}", stream, r.getId(), ex.getMessage());
            }
        }
    }

    private void ensureGroup(String stream, String group) {
        try {
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
                createGroupMkStream(connection, stream, group);
                return null;
            });
        } catch (Exception ignored) {
        }
    }

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

    private void ack(String stream, String group, MapRecord<String, Object, Object> record) {
        try {
            stringRedisTemplate.opsForStream().acknowledge(stream, group, record.getId());
        } catch (Exception ignored) {
        }
    }
}
