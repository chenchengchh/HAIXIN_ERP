package com.hxcoe.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.gateway.config.JwtConfig;
import com.hxcoe.gateway.handler.GatewayErrorResponseWriter;
import com.hxcoe.gateway.utils.JwtUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    @Test
    void shouldSkipAuthenticationForConfiguredLoginPath() {
        JwtAuthenticationFilter filter = createFilter(List.of("^/api/v1/iam/auth/login$"));
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/iam/auth/login").build()
        );

        filter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        }).block();

        assertTrue(chainCalled.get());
    }

    @Test
    void shouldSkipAuthenticationForOptionsRequest() {
        JwtAuthenticationFilter filter = createFilter(List.of("^/api/v1/iam/auth/login$"));
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/v1/douyin/tasks").build()
        );

        filter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        }).block();

        assertTrue(chainCalled.get());
    }

    @Test
    void shouldRejectProtectedPathWithoutAuthorizationHeader() {
        JwtAuthenticationFilter filter = createFilter(List.of("^/api/v1/iam/auth/login$"));
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/douyin/tasks").build()
        );

        filter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        }).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertTrue(!chainCalled.get());
    }

    private JwtAuthenticationFilter createFilter(List<String> excludePatterns) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setEnabled(true);
        jwtConfig.setHeader("Authorization");
        jwtConfig.setPrefix("Bearer");
        jwtConfig.setExcludePatterns(excludePatterns);

        ReflectionTestUtils.setField(filter, "jwtConfig", jwtConfig);
        ReflectionTestUtils.setField(filter, "jwtUtils", mock(JwtUtils.class));
        ReflectionTestUtils.setField(filter, "errorResponseWriter", new GatewayErrorResponseWriter(new ObjectMapper()));

        return filter;
    }
}
