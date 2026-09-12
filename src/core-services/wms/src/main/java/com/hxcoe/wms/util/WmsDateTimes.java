package com.hxcoe.wms.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WMS时间格式化工具：Controller手动组装Map返回时统一LocalDateTime输出格式，
 * 避免t.toString()在微秒非0时输出".610208"等不一致格式。
 */
public final class WmsDateTimes {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private WmsDateTimes() {
    }

    /**
     * 格式化LocalDateTime为"yyyy-MM-dd HH:mm:ss"，null返回空串
     */
    public static String format(LocalDateTime t) {
        if (t == null) {
            return "";
        }
        return t.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化LocalDateTime为"yyyy-MM-dd HH:mm:ss"，null返回null（保持JSON null语义）
     */
    public static String formatOrNull(LocalDateTime t) {
        if (t == null) {
            return null;
        }
        return t.format(DATETIME_FORMATTER);
    }
}
