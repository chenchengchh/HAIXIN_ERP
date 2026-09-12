package com.hxcoe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 通用Web配置类
 * 配置消息转换器，确保所有响应使用UTF-8编码
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Spring容器统一管理的ObjectMapper（应用各服务的Jackson定制配置，
     * 避免独立ObjectMapper导致日期序列化等定制失效）
     */
    private final ObjectMapper objectMapper;

    public WebConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 配置消息转换器
     * 确保能够处理各种类型的HTTP消息转换
     * @param converters 消息转换器列表
     */
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // 添加Jackson消息转换器（使用Spring管理的ObjectMapper，确保序列化定制生效）
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        // 添加String消息转换器，确保字符串响应使用UTF-8编码
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    /**
     * 配置StringHttpMessageConverter，确保字符串响应使用UTF-8编码
     */
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}