package com.hxcoe.gateway.handler;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GatewayGlobalExceptionHandler.class);
    private static final String HEADER_TRACE_ID = GatewayErrorResponseWriter.HEADER_TRACE_ID;
    private static final String HEADER_REQUEST_ID = GatewayErrorResponseWriter.HEADER_REQUEST_ID;
    private static final String LEGACY_HEADER_REQUEST_ID = "X-Request-ID";

    private final GatewayErrorResponseWriter errorResponseWriter;

    public GatewayGlobalExceptionHandler(GatewayErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatusCode statusCode = resolveStatus(ex);
        String traceId = firstNonBlank(
                exchange.getResponse().getHeaders().getFirst(HEADER_TRACE_ID),
                exchange.getRequest().getHeaders().getFirst(HEADER_TRACE_ID),
                exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID),
                exchange.getRequest().getHeaders().getFirst(LEGACY_HEADER_REQUEST_ID),
                UUID.randomUUID().toString()
        );
        String requestId = firstNonBlank(
                exchange.getResponse().getHeaders().getFirst(HEADER_REQUEST_ID),
                exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID),
                exchange.getRequest().getHeaders().getFirst(LEGACY_HEADER_REQUEST_ID),
                traceId
        );
        String message = resolveMessage(statusCode, ex);

        logger.error(
                "Gateway异常兜底 path={} status={} traceId={} requestId={} error={}",
                exchange.getRequest().getURI().getPath(),
                statusCode.value(),
                traceId,
                requestId,
                ex.getMessage(),
                ex
        );

        return errorResponseWriter.write(exchange.getResponse(), statusCode, message, traceId, requestId);
    }

    private HttpStatusCode resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getStatusCode();
        }
        if (ex instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatusCode statusCode, Throwable ex) {
        if (statusCode.value() == HttpStatus.UNAUTHORIZED.value()) {
            return GatewayErrorResponseWriter.MSG_UNAUTHORIZED;
        }
        if (statusCode.value() == HttpStatus.FORBIDDEN.value()) {
            return GatewayErrorResponseWriter.MSG_FORBIDDEN;
        }
        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            return GatewayErrorResponseWriter.MSG_NOT_FOUND;
        }
        if (statusCode.value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return GatewayErrorResponseWriter.MSG_SERVICE_UNAVAILABLE;
        }
        if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
            if (ex instanceof ResponseStatusException responseStatusException
                    && StringUtils.hasText(responseStatusException.getReason())) {
                return responseStatusException.getReason();
            }
            return GatewayErrorResponseWriter.MSG_BAD_REQUEST;
        }
        return GatewayErrorResponseWriter.MSG_INTERNAL_ERROR;
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
        return -2;
    }
}
