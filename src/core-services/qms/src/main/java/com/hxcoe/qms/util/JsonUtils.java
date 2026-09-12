package com.hxcoe.qms.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON序列化与反序列化工具类
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将对象序列化为JSON字符串
     *
     * @param value 待序列化对象
     * @return JSON字符串
     */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON序列化失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型
     * 容错处理：JSON为空或解析失败时返回null，避免单条脏数据导致整个接口500
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 目标类型
     * @return 反序列化结果，失败时返回null
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            return null;
        }
    }
}
