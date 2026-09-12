package com.hxcoe.erp.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.erp.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.erp.service.ErpPoSnapshotService;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "hxcoe.integration", name = "transport", havingValue = "REDIS_STREAM")
public class ScmPoEventRedisStreamJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTransportProperties props;

    @Autowired
    private ErpPoSnapshotService erpPoSnapshotService;

    @Scheduled(fixedDelayString = "${erp.integration.scm-po-events.poll-delay-ms:1000}", initialDelayString = "${erp.integration.scm-po-events.initial-delay-ms:30000}")
    public void poll() {
        try {
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            String stream = props.getRedis().getStreamScmPoEvents();
            String group = props.getRedis().getGroupScmPoEvents();
            String consumerName = props.getRedis().getConsumerScmPoEvents();

            createGroupIfNeeded(ops, stream, group);

            List<MapRecord<String, String, String>> records = ops.read(
                    Consumer.from(group, consumerName),
                    StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                    StreamOffset.create(stream, ReadOffset.lastConsumed())
            );
            if (records == null || records.isEmpty()) {
                return;
            }
            for (MapRecord<String, String, String> record : records) {
                String body = record.getValue().get("body");
                if (body == null) {
                    ops.acknowledge(stream, group, record.getId());
                    continue;
                }
                try {
                    PurchaseOrderEventRequest req = objectMapper.readValue(body, new TypeReference<>() {});
                    erpPoSnapshotService.applyScmPoEvent(req);
                    ops.acknowledge(stream, group, record.getId());
                } catch (Exception ex) {
                    log.warn("ERP 处理 SCM PO event 失败 recordId={} error={}", record.getId(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("ERP 轮询 SCM PO stream 失败 error={}", e.getMessage());
        }
    }

    private void createGroupIfNeeded(StreamOperations<String, String, String> ops, String stream, String group) {
        try {
            ops.createGroup(stream, ReadOffset.from("0-0"), group);
        } catch (Exception ignored) {
        }
    }
}
