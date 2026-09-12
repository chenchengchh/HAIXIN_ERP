package com.hxcoe.oa.config;

import org.springframework.context.annotation.Configuration;

/**
 * 模块集成配置类。
 *
 * RestTemplate 已统一在 {@link RestClientConfig} 中定义，
 * 这里不再重复注册，避免 Bean 名称冲突导致应用启动失败。
 */
@Configuration
public class IntegrationConfig {
}
