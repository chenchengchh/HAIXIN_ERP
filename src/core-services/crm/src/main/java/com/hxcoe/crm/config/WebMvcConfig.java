package com.hxcoe.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * WebMvc配置
 * 确保所有HTTP消息转换器都使用UTF-8编码
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Spring容器统一管理的ObjectMapper（应用JacksonConfig的日期格式化等定制，
     * 避免独立ObjectMapper导致序列化定制失效）
     */
    private final ObjectMapper objectMapper;

    public WebMvcConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
        // 配置String消息转换器为UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(0, stringConverter);

        // 配置Jackson JSON消息转换器为UTF-8（使用Spring管理的ObjectMapper）
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(1, jsonConverter);
    }
}
