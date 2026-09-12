package com.hxcoe.oa.iam.audit;

import com.hxcoe.oa.iam.entity.OaAuditActionEntity;
import com.hxcoe.oa.iam.repository.OaAuditActionRepository;
import com.hxcoe.oa.iam.security.OaPrincipal;
import com.hxcoe.oa.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class OaAuditActionInterceptor implements HandlerInterceptor {

    private static final String ATTR_START_MS = "oa.audit.startMs";

    private final OaAuditActionRepository oaAuditActionRepository;

    public OaAuditActionInterceptor(OaAuditActionRepository oaAuditActionRepository) {
        this.oaAuditActionRepository = oaAuditActionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (method != null && !method.isBlank() && !"GET".equalsIgnoreCase(method)) {
            request.setAttribute(ATTR_START_MS, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        String method = request.getMethod();
        if (method == null || method.isBlank() || "GET".equalsIgnoreCase(method)) {
            return;
        }
        try {
            Long startMs = null;
            Object attr = request.getAttribute(ATTR_START_MS);
            if (attr instanceof Long v) {
                startMs = v;
            } else if (attr != null) {
                try {
                    startMs = Long.parseLong(String.valueOf(attr));
                } catch (Exception ignore) {
                    startMs = null;
                }
            }
            long durationMs = startMs == null ? -1L : Math.max(0L, System.currentTimeMillis() - startMs);
            String traceId = String.valueOf(request.getAttribute(TraceIdFilter.MDC_TRACE_ID));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            Long employeeId = null;
            String username = null;
            if (auth != null && auth.getPrincipal() instanceof OaPrincipal p) {
                userId = p.getUserId();
                employeeId = p.getEmployeeId();
                username = p.getUsername();
            } else if (auth != null) {
                username = String.valueOf(auth.getPrincipal());
            }

            OaAuditActionEntity a = new OaAuditActionEntity();
            a.setUserId(userId);
            a.setEmployeeId(employeeId);
            a.setUsername(username);
            a.setTraceId(traceId == null || "null".equals(traceId) ? null : traceId);
            a.setHttpMethod(method);
            a.setPath(request.getRequestURI());
            a.setStatusCode(response.getStatus());
            a.setDurationMs(durationMs);
            a.setResult(ex == null && response.getStatus() < 400 ? "OK" : "FAIL");
            a.setErrorMessage(ex == null ? null : trimTo512(ex.getMessage()));

            oaAuditActionRepository.save(a);
        } catch (Exception writeEx) {
            log.warn("OA 操作审计写入失败 error={}", writeEx.getMessage());
        }
    }

    private String trimTo512(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= 512) {
            return s;
        }
        return s.substring(0, 512);
    }
}
