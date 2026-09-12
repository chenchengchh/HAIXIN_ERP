package com.hxcoe.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * 统一维护请求上下文，确保日志和响应都可携带可检索字段。
 */
public final class RequestContextSupport {

    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String LEGACY_HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_EVENT_ID = "X-Event-Id";

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_SERVICE = "service";
    public static final String MDC_EVENT_ID = "eventId";

    private static final String UNKNOWN_SERVICE = "unknown-service";
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> SERVICE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> EVENT_ID_HOLDER = new ThreadLocal<>();

    private RequestContextSupport() {
    }

    public static void bind(HttpServletRequest request, HttpServletResponse response, String serviceName) {
        String traceId = firstNonBlank(
                request.getHeader(HEADER_TRACE_ID),
                request.getHeader(HEADER_REQUEST_ID),
                request.getHeader(LEGACY_HEADER_REQUEST_ID),
                UUID.randomUUID().toString()
        );
        String requestId = firstNonBlank(
                request.getHeader(HEADER_REQUEST_ID),
                request.getHeader(LEGACY_HEADER_REQUEST_ID),
                traceId
        );
        String userId = firstNonBlank(request.getHeader(HEADER_USER_ID), "");
        String eventId = firstNonBlank(request.getHeader(HEADER_EVENT_ID), "");
        String service = firstNonBlank(serviceName, UNKNOWN_SERVICE);

        request.setAttribute(MDC_TRACE_ID, traceId);
        request.setAttribute(MDC_REQUEST_ID, requestId);
        request.setAttribute(MDC_USER_ID, userId);
        request.setAttribute(MDC_SERVICE, service);
        request.setAttribute(MDC_EVENT_ID, eventId);

        TRACE_ID_HOLDER.set(traceId);
        REQUEST_ID_HOLDER.set(requestId);
        USER_ID_HOLDER.set(userId);
        SERVICE_HOLDER.set(service);
        EVENT_ID_HOLDER.set(eventId);

        response.setHeader(HEADER_TRACE_ID, traceId);
        response.setHeader(HEADER_REQUEST_ID, requestId);

        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_USER_ID, userId);
        MDC.put(MDC_SERVICE, service);
        if (StringUtils.hasText(eventId)) {
            MDC.put(MDC_EVENT_ID, eventId);
        } else {
            MDC.remove(MDC_EVENT_ID);
        }
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
        REQUEST_ID_HOLDER.remove();
        USER_ID_HOLDER.remove();
        SERVICE_HOLDER.remove();
        EVENT_ID_HOLDER.remove();
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_SERVICE);
        MDC.remove(MDC_EVENT_ID);
    }

    public static String currentTraceId() {
        return firstNonBlank(TRACE_ID_HOLDER.get(), MDC.get(MDC_TRACE_ID), "");
    }

    public static String currentRequestId() {
        return firstNonBlank(REQUEST_ID_HOLDER.get(), MDC.get(MDC_REQUEST_ID), "");
    }

    public static String currentUserId() {
        return firstNonBlank(USER_ID_HOLDER.get(), MDC.get(MDC_USER_ID), "");
    }

    public static String currentService() {
        return firstNonBlank(SERVICE_HOLDER.get(), MDC.get(MDC_SERVICE), UNKNOWN_SERVICE);
    }

    public static String currentEventId() {
        return firstNonBlank(EVENT_ID_HOLDER.get(), MDC.get(MDC_EVENT_ID), "");
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return "";
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return "";
    }
}
