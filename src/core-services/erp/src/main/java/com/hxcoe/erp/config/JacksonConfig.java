package com.hxcoe.erp.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson配置：统一LocalDateTime/LocalDate序列化为"yyyy-MM-dd HH:mm:ss"/"yyyy-MM-dd"。
 * 反序列化采用容错模式：同时兼容"yyyy-MM-dd HH:mm:ss"（前端/内部服务格式）
 * 与ISO格式"yyyy-MM-dd'T'HH:mm:ss"（跨模块调用方格式），避免财务建单等接口400/500。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 容错LocalDateTime反序列化器：优先按"yyyy-MM-dd HH:mm:ss"解析，失败时回退ISO格式
     */
    static class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            text = text.trim();
            try {
                return LocalDateTime.parse(text, DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                // 回退ISO格式（如2026-08-01T12:00:00）
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
    }

    /**
     * 注册统一的序列化器与容错反序列化器
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        // 注意：必须使用deserializerByType显式指定类型，
        // 因为builder.deserializers()依赖handledType()，自定义反序列化器未声明时会抛Unknown handled type
        return builder -> builder
                .serializers(new LocalDateTimeSerializer(DATETIME_FORMATTER))
                .serializers(new LocalDateSerializer(DATE_FORMATTER))
                .deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer())
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
    }
}
