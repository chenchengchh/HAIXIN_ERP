package com.hxcoe.gateway.filter;

import com.hxcoe.gateway.handler.GatewayErrorResponseWriter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.regex.Pattern;

/**
 * JWT认证过滤器，用于拦截请求并验证JWT令牌
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String LEGACY_HEADER_REQUEST_ID = "X-Request-ID";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLE = "X-Role";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Autowired
    private com.hxcoe.gateway.utils.JwtUtils jwtUtils;

    @Autowired
    private com.hxcoe.gateway.config.JwtConfig jwtConfig;

    @Autowired
    private GatewayErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        String traceId = firstNonBlank(
                request.getHeaders().getFirst(HEADER_TRACE_ID),
                request.getHeaders().getFirst(HEADER_REQUEST_ID),
                request.getHeaders().getFirst(LEGACY_HEADER_REQUEST_ID),
                UUID.randomUUID().toString()
        );
        String requestId = firstNonBlank(
                request.getHeaders().getFirst(HEADER_REQUEST_ID),
                request.getHeaders().getFirst(LEGACY_HEADER_REQUEST_ID),
                traceId
        );
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(HEADER_TRACE_ID, traceId);
        response.getHeaders().set(HEADER_REQUEST_ID, requestId);

        ServerHttpRequest.Builder baseRequestBuilder = request.mutate()
                .header(HEADER_TRACE_ID, traceId)
                .header(HEADER_REQUEST_ID, requestId);
        ServerWebExchange preparedExchange = exchange.mutate().request(baseRequestBuilder.build()).build();

        if (!jwtConfig.isEnabled()) {
            return chain.filter(preparedExchange);
        }

        // 检查是否需要跳过认证
        if (shouldSkipAuthentication(request, path)) {
            logger.debug("跳过认证路径 path={} traceId={} requestId={}", path, traceId, requestId);
            return chain.filter(preparedExchange);
        }

        // 获取Authorization头
        HttpHeaders headers = preparedExchange.getRequest().getHeaders();
        String authorizationHeader = headers.getFirst(jwtConfig.getHeader());

        // 检查Authorization头是否存在
        if (authorizationHeader == null || !authorizationHeader.startsWith(jwtConfig.getPrefix() + " ")) {
            logger.warn("Authorization头不存在或格式错误 path={} traceId={} requestId={}", path, traceId, requestId);
            return unauthorized(preparedExchange, traceId, requestId, GatewayErrorResponseWriter.MSG_UNAUTHORIZED);
        }

        // 提取JWT令牌
        String token = authorizationHeader.substring(jwtConfig.getPrefix().length() + 1);

        // 验证JWT令牌
        try {
            if (!jwtUtils.validateToken(token)) {
                logger.warn("JWT令牌无效 path={} traceId={} requestId={}", path, traceId, requestId);
                return unauthorized(preparedExchange, traceId, requestId, GatewayErrorResponseWriter.MSG_UNAUTHORIZED);
            }

            // 从JWT令牌中获取用户信息
            String username = jwtUtils.getUsernameFromToken(token);
            Long userId = jwtUtils.getUserIdFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            String roles = String.join(",", jwtUtils.getRolesFromToken(token));
            String permissions = String.join(",", jwtUtils.getPermissionsFromToken(token));

            if (userId == null || !StringUtils.hasText(username)) {
                logger.warn("JWT缺少必要用户声明 path={} traceId={} requestId={}", path, traceId, requestId);
                return unauthorized(preparedExchange, traceId, requestId, GatewayErrorResponseWriter.MSG_UNAUTHORIZED);
            }

            // 将用户信息添加到请求头中
            ServerHttpRequest.Builder requestBuilder = preparedExchange.getRequest().mutate()
                    .header(HEADER_TRACE_ID, traceId)
                    .header(HEADER_REQUEST_ID, requestId)
                    .header(HEADER_USER_ID, userId.toString())
                    .header(HEADER_USERNAME, username);

            if (StringUtils.hasText(role)) {
                requestBuilder.header(HEADER_ROLE, role);
            }
            if (StringUtils.hasText(roles)) {
                requestBuilder.header(HEADER_ROLES, roles);
            }
            if (StringUtils.hasText(permissions)) {
                requestBuilder.header(HEADER_PERMISSIONS, permissions);
            }

            logger.debug("JWT认证成功 path={} userId={} username={} role={} traceId={} requestId={}",
                    path, userId, username, role, traceId, requestId);

            // 继续处理请求
            return chain.filter(preparedExchange.mutate().request(requestBuilder.build()).build());
        } catch (Exception e) {
            logger.error("JWT认证失败 path={} traceId={} requestId={} error={}",
                    path, traceId, requestId, e.getMessage(), e);
            return unauthorized(preparedExchange, traceId, requestId, GatewayErrorResponseWriter.MSG_UNAUTHORIZED);
        }
    }

    /**
     * 检查是否需要跳过认证
     * @param path 请求路径
     * @return 是否需要跳过认证
     */
    private boolean shouldSkipAuthentication(ServerHttpRequest request, String path) {
        // OPTIONS预检请求直接放行
        if (request != null && request.getMethod() == HttpMethod.OPTIONS) {
            return true;
        }
        // WebSocket升级请求放行（WebSocket无法在握手阶段携带Authorization头）
        if (request != null && isWebSocketUpgrade(request)) {
            logger.debug("WebSocket升级请求跳过认证 path={}", path);
            return true;
        }
        if (jwtConfig.getExcludePatterns() != null) {
            for (String p : jwtConfig.getExcludePatterns()) {
                if (p == null || p.isBlank()) {
                    continue;
                }
                if (Pattern.compile(p).matcher(path).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否为WebSocket升级请求
     * @param request HTTP请求
     * @return 是否为WebSocket升级请求
     */
    private boolean isWebSocketUpgrade(ServerHttpRequest request) {
        String upgrade = request.getHeaders().getFirst("Upgrade");
        return upgrade != null && "websocket".equalsIgnoreCase(upgrade.trim());
    }
    
    /**
     * 返回未授权响应
     * @param exchange ServerWebExchange
     * @return Mono<Void>
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String traceId, String requestId, String message) {
        ServerHttpResponse response = exchange.getResponse();
        return errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, message, traceId, requestId);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    @Override
    public int getOrder() {
        // 设置过滤器优先级，确保在其他过滤器之前执行
        return -1;
    }
}
