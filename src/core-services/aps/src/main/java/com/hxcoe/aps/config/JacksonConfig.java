package com.hxcoe.aps.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Jackson配置类
 * 确保JSON序列化使用UTF-8编码
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    /**
     * 配置ObjectMapper
     * 确保JSON序列化使用UTF-8编码
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 关闭日期时间序列化作为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 设置非空字段才序列化
        objectMapper.setDefaultPropertyInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    /**
     * 配置消息转换器
     * 确保所有消息转换器都使用UTF-8编码
     */
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // 1. 替换或添加StringHttpMessageConverter，确保字符串响应使用UTF-8编码
        boolean hasStringConverter = false;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                converters.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                hasStringConverter = true;
                break;
            }
        }
        if (!hasStringConverter) {
            converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        }
        
        // 2. 替换或添加MappingJackson2HttpMessageConverter，确保JSON响应使用UTF-8编码
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper());
        jacksonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        
        // 支持所有JSON相关媒体类型
        List<MediaType> jacksonMediaTypes = new ArrayList<>();
        jacksonMediaTypes.add(MediaType.APPLICATION_JSON);
        jacksonMediaTypes.add(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        jacksonMediaTypes.add(MediaType.TEXT_PLAIN);
        jacksonMediaTypes.add(MediaType.TEXT_HTML);
        jacksonConverter.setSupportedMediaTypes(jacksonMediaTypes);
        
        boolean hasJacksonConverter = false;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                converters.set(i, jacksonConverter);
                hasJacksonConverter = true;
                break;
            }
        }
        if (!hasJacksonConverter) {
            converters.add(1, jacksonConverter);
        }
    }
}
