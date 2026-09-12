package com.hxcoe.common.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.web.RequestContextSupport;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class ResultSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        RequestContextSupport.clear();
    }

    @Test
    void shouldSerializeMsgAndContextFields() throws Exception {
        MDC.put(RequestContextSupport.MDC_TRACE_ID, "trace-001");
        MDC.put(RequestContextSupport.MDC_REQUEST_ID, "req-001");
        MDC.put(RequestContextSupport.MDC_SERVICE, "srm-service");

        Result<Map<String, Object>> result = Result.success("请求成功", Map.of("id", 1L));

        String json = objectMapper.writeValueAsString(result);

        assertTrue(json.contains("\"msg\":\"请求成功\""));
        assertFalse(json.contains("\"message\""));
        assertTrue(json.contains("\"traceId\":\"trace-001\""));
        assertTrue(json.contains("\"requestId\":\"req-001\""));
        assertTrue(json.contains("\"service\":\"srm-service\""));
    }

    @Test
    void shouldAcceptLegacyMessageFieldWhenDeserializing() throws Exception {
        Result<?> result = objectMapper.readValue(
                "{\"code\":400,\"message\":\"参数错误\",\"data\":null}",
                Result.class
        );

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertEquals("参数错误", result.getMsg());
    }
}
