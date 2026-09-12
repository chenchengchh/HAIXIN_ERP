package com.hxcoe.qms.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置：统一Java 8日期时间类型的序列化格式，
 * 避免LocalDateTime输出为数组，强制使用 "yyyy-MM-dd HH:mm:ss" 字符串
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期格式：yyyy-MM-dd
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 自定义ObjectMapper构建器，注册LocalDateTime/LocalDate的格式化序列化器与反序列化器
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder
                .serializers(new LocalDateTimeSerializer(DATETIME_FORMATTER))
                .serializers(new LocalDateSerializer(DATE_FORMATTER))
                .deserializers(new LocalDateTimeDeserializer(DATETIME_FORMATTER))
                .deserializers(new LocalDateDeserializer(DATE_FORMATTER));
    }
}
