package com.hxcoe.oa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * 数据清理工具类
 * 用于清理返回给前端的数据，确保不包含HTML注释或非法字符
 */
public class DataCleanUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanUtils.class);

    /**
     * 清理对象中的所有字段
     * @param obj 要清理的对象
     * @param <T> 对象类型
     * @return 清理后的对象
     */
    public static <T> T cleanObject(T obj) {
        if (obj == null) {
            return null;
        }

        // 处理基本类型
        if (obj instanceof String) {
            return (T) cleanString((String) obj);
        }
        if (obj instanceof Number || obj instanceof Boolean || obj instanceof Character) {
            return obj;
        }

        // 处理集合类型
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            Collection<Object> cleanedCollection = new ArrayList<>();
            for (Object item : collection) {
                cleanedCollection.add(cleanObject(item));
            }
            return (T) cleanedCollection;
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Map<Object, Object> cleanedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String cleanedKey = cleanString(String.valueOf(entry.getKey()));
                Object cleanedValue = cleanObject(entry.getValue());
                cleanedMap.put(cleanedKey, cleanedValue);
            }
            return (T) cleanedMap;
        }
        if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            Object[] cleanedArray = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                cleanedArray[i] = cleanObject(array[i]);
            }
            return (T) cleanedArray;
        }

        // 注意：移除了反射实现，因为反射会带来性能开销
        // 建议在Controller层直接返回Map或DTO对象，避免使用复杂的自定义对象
        // 或者在Service层将实体对象转换为DTO对象
        logger.warn("DataCleanUtils: 不支持的对象类型，返回原始对象: {}", obj.getClass().getName());
        
        return obj;
    }

    /**
     * 清理字符串，确保不包含HTML注释或非法字符
     * @param str 要清理的字符串
     * @return 清理后的字符串
     */
    public static String cleanString(String str) {
        if (str == null) {
            return null;
        }

        // 移除HTML注释
        String cleaned = str.replaceAll("<!--[\\s\\S]*?-->", "");

        // 移除非法字符
        cleaned = cleaned.replaceAll("[<>'\"&]", "");

        // 移除控制字符
        cleaned = cleaned.replaceAll("[\\x00-\\x1F\\x7F]", "");

        // 移除多余空格
        cleaned = cleaned.trim();

        return cleaned;
    }

    /**
     * 验证字符串是否合法
     * @param str 要验证的字符串
     * @return 是否合法
     */
    public static boolean isValidString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return true;
        }

        // 检查是否包含HTML注释
        if (str.contains("<!--") || str.contains("-->") || str.contains("<html") || str.contains("<!DOCTYPE")) {
            return false;
        }

        // 检查是否包含非法字符
        if (str.matches(".*[<>'\"&].*")) {
            return false;
        }

        return true;
    }
}