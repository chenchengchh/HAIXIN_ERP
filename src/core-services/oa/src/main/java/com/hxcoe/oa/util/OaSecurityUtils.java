package com.hxcoe.oa.util;

import com.hxcoe.oa.iam.security.OaPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * OA安全上下文工具类。
 * <p>从SecurityContextHolder解析当前登录用户（OaPrincipal），
 * 供各业务Service在创建实体时自动填充创建人/组织者字段，
 * 避免完全依赖客户端传参导致的非空约束违约（500错误）。</p>
 */
public final class OaSecurityUtils {

    private OaSecurityUtils() {
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 用户ID，未登录或匿名时返回null
     */
    public static Long getCurrentUserId() {
        OaPrincipal principal = getCurrentPrincipal();
        return principal != null ? principal.getUserId() : null;
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名，未登录或匿名时返回null
     */
    public static String getCurrentUsername() {
        OaPrincipal principal = getCurrentPrincipal();
        return principal != null ? principal.getUsername() : null;
    }

    /**
     * 获取当前登录用户Principal。
     *
     * @return OaPrincipal实例，未登录或非OaPrincipal类型时返回null
     */
    public static OaPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OaPrincipal principal) {
            return principal;
        }
        return null;
    }
}
