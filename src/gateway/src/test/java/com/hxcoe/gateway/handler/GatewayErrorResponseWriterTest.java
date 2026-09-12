package com.hxcoe.gateway.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

class GatewayErrorResponseWriterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldWriteApiResponseShapeForUnauthorized() throws Exception {
        GatewayErrorResponseWriter writer = new GatewayErrorResponseWriter(objectMapper);
        MockServerHttpResponse response = new MockServerHttpResponse();

        writer.write(response, HttpStatus.UNAUTHORIZED, "未登录", "trace-1", "request-1").block();

        JsonNode body = objectMapper.readTree(response.getBodyAsString().block());
        assertEquals(401, body.get("code").asInt());
        assertEquals("未登录", body.get("msg").asText());
        assertNull(body.get("timestamp"));
        assertEquals("trace-1", body.get("traceId").asText());
        assertEquals("request-1", body.get("requestId").asText());
        assertEquals("gateway", body.get("service").asText());
    }
}
