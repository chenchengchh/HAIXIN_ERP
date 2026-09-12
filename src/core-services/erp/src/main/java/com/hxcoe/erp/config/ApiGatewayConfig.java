package com.hxcoe.erp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nonnull;

/**
 * API网关配置类
 * 配置跨域、路由、限流等API网关功能
 */
@Configuration
public class ApiGatewayConfig implements WebMvcConfigurer {

    /**
     * 配置跨域资源共享
     */
    @Override
    public void addCorsMappings(@Nonnull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置API限流
     * 这里可以添加限流相关的Bean配置
     */
    // @Bean
    // public RateLimiter rateLimiter() {
    //     // 实现API限流逻辑
    //     return new RateLimiter();
    // }

    /**
     * 配置API监控
     * 这里可以添加API监控相关的Bean配置
     */
    // @Bean
    // public ApiMonitor apiMonitor() {
    //     // 实现API监控逻辑
    //     return new ApiMonitor();
    // }
}
