package com.hxcoe.common.api;

import java.util.Map;

/**
 * 兼容历史 Result(0=成功) 与 ApiResponse(200=成功) 的轻量判定工具。
 */
public final class ResponseStatusAdapter {

    private ResponseStatusAdapter() {
    }

    public static boolean isSuccess(Integer code) {
        return code != null && (code == 0 || code == 200);
    }

    public static boolean isSuccess(Map<?, ?> raw) {
        return isSuccess(extractCode(raw));
    }

    public static Integer extractCode(Map<?, ?> raw) {
        if (raw == null) {
            return null;
        }
        Object code = raw.get("code");
        if (code instanceof Number number) {
            return number.intValue();
        }
        if (code instanceof String value) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ignore) {
                return null;
            }
        }
        return null;
    }

    public static String extractMessage(Map<?, ?> raw) {
        if (raw == null) {
            return null;
        }
        Object msg = raw.get("msg");
        if (msg instanceof String value && !value.isBlank()) {
            return value;
        }
        Object message = raw.get("message");
        if (message instanceof String value && !value.isBlank()) {
            return value;
        }
        return null;
    }
}
