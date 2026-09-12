package com.hxcoe.common.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控配置类
 */
@Configuration
public class MonitoringConfig {

    /**
     * 自定义Meter Registry，添加通用标签和过滤器
     *
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer() {
        return registry -> registry.config()
                .commonTags("application", "system", "environment", "dev")
                .meterFilter(MeterFilter.denyNameStartsWith("jvm.classes"))
                .meterFilter(MeterFilter.denyNameStartsWith("jvm.gc"))
                .meterFilter(MeterFilter.denyNameStartsWith("jvm.memory"))
                .meterFilter(MeterFilter.denyNameStartsWith("jvm.threads"))
                .meterFilter(MeterFilter.denyNameStartsWith("logback"))
                .meterFilter(MeterFilter.denyNameStartsWith("process"));
    }

    /**
     * 启用@Timed注解支持
     *
     * @param registry MeterRegistry
     * @return TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 启用@Counted注解支持
     *
     * @param registry MeterRegistry
     * @return CountedAspect
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
