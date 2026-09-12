package com.hxcoe.common.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 为未显式配置日志模板的服务提供统一的上下文字段输出格式。
 */
public class RequestLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "hxcoeRequestLoggingDefaults";
    private static final String DEFAULT_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [service:%X{service:-},traceId:%X{traceId:-},requestId:%X{requestId:-},userId:%X{userId:-},eventId:%X{eventId:-}] %logger{36} - %msg%n";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = new LinkedHashMap<>();

        putIfMissing(environment, defaults, "logging.pattern.console", DEFAULT_PATTERN);
        putIfMissing(environment, defaults, "logging.pattern.file", DEFAULT_PATTERN);
        putIfMissing(environment, defaults, "logging.pattern.level",
                "%5p [service:%X{service:-},traceId:%X{traceId:-},requestId:%X{requestId:-},userId:%X{userId:-},eventId:%X{eventId:-}]");

        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }

    private void putIfMissing(
            ConfigurableEnvironment environment,
            Map<String, Object> defaults,
            String key,
            String value
    ) {
        if (environment.getProperty(key) == null) {
            defaults.put(key, value);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
