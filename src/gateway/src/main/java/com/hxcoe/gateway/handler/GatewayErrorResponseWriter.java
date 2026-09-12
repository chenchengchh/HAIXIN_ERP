package com.hxcoe.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GatewayErrorResponseWriter {

    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final MediaType APPLICATION_JSON_UTF8 = MediaType.parseMediaType("application/json;charset=UTF-8");
    public static final String MSG_UNAUTHORIZED = "未登录";
    public static final String MSG_FORBIDDEN = "无权限";
    public static final String MSG_NOT_FOUND = "数据不存在";
    public static final String MSG_BAD_REQUEST = "参数错误";
    public static final String MSG_INTERNAL_ERROR = "系统错误";
    public static final String MSG_SERVICE_UNAVAILABLE = "服务不可用";

    private static final Logger logger = LoggerFactory.getLogger(GatewayErrorResponseWriter.class);

    private final ObjectMapper objectMapper;

    public GatewayErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(
            ServerHttpResponse response,
            HttpStatusCode statusCode,
            String message,
            String traceId,
            String requestId
    ) {
        response.setStatusCode(statusCode);
        response.getHeaders().setContentType(APPLICATION_JSON_UTF8);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8.toString());
        response.getHeaders().set(HEADER_TRACE_ID, traceId);
        response.getHeaders().set(HEADER_REQUEST_ID, requestId);

        ApiResponse<Void> body = createBody(statusCode, message, traceId, requestId);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception ex) {
            logger.error("Gateway错误响应序列化失败 traceId={} requestId={}", traceId, requestId, ex);
            byte[] bytes = (
                    "{\"code\":" + statusCode.value()
                            + ",\"msg\":\"" + message
                            + "\",\"data\":null,\"traceId\":\"" + traceId
                            + "\",\"requestId\":\"" + requestId
                            + "\",\"service\":\"gateway\"}"
            ).getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    private ApiResponse<Void> createBody(HttpStatusCode statusCode, String message, String traceId, String requestId) {
        int responseCode = statusCode.value();
        String normalizedMessage = StringUtils.hasText(message) ? message : resolveMessage(statusCode);
        return new ApiResponse<>(
                responseCode,
                normalizedMessage,
                null,
                traceId,
                requestId,
                "gateway"
        );
    }

    private String resolveMessage(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> MSG_BAD_REQUEST;
            case 401 -> MSG_UNAUTHORIZED;
            case 403 -> MSG_FORBIDDEN;
            case 404 -> MSG_NOT_FOUND;
            case 503 -> MSG_SERVICE_UNAVAILABLE;
            default -> MSG_INTERNAL_ERROR;
        };
    }
}
